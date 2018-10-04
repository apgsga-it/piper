package com.apgsga.patch.service.client.revision

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
	
	def addRevision(def target, def revision) {
		def revFileAsJson = new JsonSlurper().parse(revisionFile)
		if(revFileAsJson."${target}" == null) {
			def builder = new JsonBuilder(revFileAsJson)
			builder{
				"${target}"{
					lastRevision(revision)
					revisions([revision])
				}
			}
			addNewContentToExistingRevisionFile(builder)
		}
		else {
			revFileAsJson."${target}".revisions.add(revision)
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
				lastProdRev("SNAPSHOT")
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
	
	def setProductionRevision(def revision) {
		def revFileAsJson = new JsonSlurper().parse(revisionFile)
		revFileAsJson.lastProdRev = revision
		revisionFile.write(new JsonBuilder(revFileAsJson).toPrettyString())
	}
	
	def getProductionRevision() {
		def revFileAsJson = new JsonSlurper().parse(revisionFile)
		println revFileAsJson.lastProdRev
	}
	
	def resetRevisions(def target) {
		def revFileAsJson = new JsonSlurper().parse(revisionFile)
		if(revFileAsJson."${target}" != null) {
			revFileAsJson."${target}".revisions = []
			//TODO JHE : to be verified, do we always want to set it with lastProdRev?
			revFileAsJson."${target}".lastRevision = revFileAsJson.lastProdRev
			revisionFile.write(new JsonBuilder(revFileAsJson).toPrettyString())
		}
	}
	 
}
