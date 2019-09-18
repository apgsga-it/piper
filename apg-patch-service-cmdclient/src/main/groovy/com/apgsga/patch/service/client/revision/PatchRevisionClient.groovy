package com.apgsga.patch.service.client.revision

import java.util.stream.Collectors

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

class PatchRevisionClient {
	
	private config
	
	private revisionFile
	
	public PatchRevisionClient(def configuration) {
		config = configuration
		initRevisionFile()
	}
	
	def addRevision(def target, def revision, def fullRevisionPrefix) {
		def revFileAsJson = new JsonSlurper().parse(revisionFile)
		if(revFileAsJson."${target}" == null) {
			def builder = new JsonBuilder(revFileAsJson)
			builder{
				"${target}"{
					lastRevision(revision)
					revisions(["${fullRevisionPrefix}${revision}"])
				}
			}
			addNewContentToExistingRevisionFile(builder)
		}
		else {
			revFileAsJson."${target}".revisions.add("${fullRevisionPrefix}${revision}")
			revFileAsJson."${target}".lastRevision = revision
			revisionFile.write(new JsonBuilder(revFileAsJson).toPrettyString())
		}
	}
	
	private def addNewContentToExistingRevisionFile(JsonBuilder builder) {
		def revFileAsJson = new JsonSlurper().parse(revisionFile)
		revisionFile.write(JsonOutput.prettyPrint(JsonOutput.toJson(revFileAsJson + builder.content)))
	}
	
	def getInstalledRevisions(def target) {
		def revFileAsJson = new JsonSlurper().parse(revisionFile)
		if(revFileAsJson."${target}" != null) {
			return revFileAsJson."${target}".revisions
		}
		else {
			return null
		}
		
	}
	
	def nextRevision() {
		def revFileAsJson = new JsonSlurper().parse(revisionFile)
		println revFileAsJson.nextRev
		incrementNextRev()
	}
	
	private def incrementNextRev() {
		def revFileAsJson = new JsonSlurper().parse(revisionFile)
		def currentRev = revFileAsJson.nextRev
		currentRev++
		revFileAsJson.nextRev = currentRev
		revisionFile.write(new JsonBuilder(revFileAsJson).toPrettyString())
	}
	
	def initRevisionFile() {
		revisionFile = new File(config.revision.file.path)
		if(!revisionFile.exists()) {
			def builder = new JsonBuilder()
			builder {
				nextRev(1)
			}
			revisionFile.write(builder.toPrettyString())
		}
	}
	
	def lastRevision(def target) {
		def revFileAsJson = new JsonSlurper().parse(revisionFile)
		if(revFileAsJson."${target}" != null)
			println revFileAsJson."${target}".lastRevision
		else {
			println "SNAPSHOT"
		}
	}
	
	def resetRevisions(def source, def target) {
		def revFileAsJson = new JsonSlurper().parse(revisionFile)
		if(revFileAsJson."${target}" != null) {
			if(revFileAsJson."${source}" != null) {
				revFileAsJson."${target}".revisions = []
				revFileAsJson."${target}".lastRevision = revFileAsJson."${source}".lastRevision
				revisionFile.write(new JsonBuilder(revFileAsJson).toPrettyString())
			}
			else {
				revFileAsJson.remove(target)
				revisionFile.write(new JsonBuilder(revFileAsJson).toPrettyString())
			}
		}
	}
	
	def getRevisions(def target) {
		def revisionsList = []
		def revFileAsJson = new JsonSlurper().parse(revisionFile)
		if(revFileAsJson."${target}" != null) {
			revisionsList = revFileAsJson."${target}".revisions.stream().collect(Collectors.toList())
		}
		if(revisionsList.isEmpty()) {
			print ""
		}
		else {
			println revisionsList.join(",")
		}
	}
	
	def deleteRevisions(def target) {
		assert !isProd(target) : "Revisions can't be deleted for production target: ${target}"
		def revFileAsJson = new JsonSlurper().parse(revisionFile)
		if(revFileAsJson."${target}" != null) {
			revFileAsJson."${target}".revisions = []
			revisionFile.write(new JsonBuilder(revFileAsJson).toPrettyString())
		}
	}
	
	def isProd(def target) {
		def isProd = false
		def targetSystemMappingFilePath = "${config.config.dir}/${config.target.system.mapping.file.name}"
		def targetSystemMappingFile = new File(targetSystemMappingFilePath)
		assert targetSystemMappingFile.exists() : "${config.config.dir}/${config.target.system.mapping.file.name} does not exist!"
		def targetSystemMappingAsJson = new JsonSlurper().parse(targetSystemMappingFile)
		targetSystemMappingAsJson.targetSystems.each{targetSystem ->
			if(targetSystem.target.equalsIgnoreCase(target)) {
				isProd = targetSystem.name.equalsIgnoreCase("produktion")
				return // exit closure
			}
		}
		return isProd
	}
}
