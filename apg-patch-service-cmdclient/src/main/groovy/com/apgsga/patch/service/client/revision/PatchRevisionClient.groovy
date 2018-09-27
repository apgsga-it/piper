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
	
	private def initRevisionFile() {
		revisionFile = new File(config.revision.file.path)
		if(!revisionFile.exists()) {
			def builder = new JsonBuilder()
			builder {
				lastProdRev(null)
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
			println "null"
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
	
//	def retrieveRevisions(def targetInd, def installationTarget) {
//		
//		println("(retrieveRevisions) Retrieving revision for target ${installationTarget} (${targetInd})")
//
//		def revisionFile = new File(config.revision.file.path)
//		def currentRevision = [P:1,T:10000]
//		def lastRevision = [:]
//		def revisions = [lastRevisions:lastRevision, currentRevision:currentRevision]
//		def patchRevision
//		def patchLastRevision
//		if (revisionFile.exists()) {
//			revisions = new JsonSlurper().parseText(revisionFile.text)
//			println("(retrieveRevisions) Revision file exist and following content has been parsed: ${revisions}")
//		}
//
//		if(targetInd.equals("P")) {
//			patchRevision = revisions.currentRevision[targetInd]
//			println("(retrieveRevisions) targetInd = P ... patchRevision = ${patchRevision}")
//		}
//		else {
//			if(revisions.lastRevisions.get(installationTarget) == null) {
//				patchRevision = revisions.currentRevision[targetInd]
//				println("(retrieveRevisions) Revision for ${installationTarget} was null, patchRevision will be ${patchRevision}")
//			}
//			else {
//				def currentLastRevision = revisions.lastRevisions.get(installationTarget)
//				println("(retrieveRevisions) currentLastRevision = ${currentLastRevision}")
//				if(currentLastRevision.endsWith("@P")) {
//					patchRevision = currentLastRevision.substring(0, currentLastRevision.size()-2)
//					patchLastRevision = "CLONED"
//					println("(retrieveRevisions) New patch for ${installationTarget} after clone")
//					println("(retrieveRevisions) patchRevision = ${patchRevision} / patchLastRevision = ${patchLastRevision}")
//				}
//				else {
//					patchRevision = (revisions.lastRevisions.get(installationTarget)).toInteger() + 1
//					println("(retrieveRevisions) patchRevision = ${patchRevision}")
//				}
//			}
//		}
//		
//		if(patchLastRevision == null) {
//			patchLastRevision = revisions.lastRevisions.get(installationTarget,'SNAPSHOT')
//		}
//
//		println("(retrieveRevisions) patchLastRevision = ${patchLastRevision}")
//		// JHE (31.05.2018) : we print the json on stdout so that the pipeline can get and parse it. Unfortunately there is currently no supported alternative: https://issues.jenkins-ci.org/browse/JENKINS-26133
//		def json = JsonOutput.toJson([fromRetrieveRevision:[revision: patchRevision, lastRevision: patchLastRevision]])
//		println json
//	}
//	
//	def retrieveLastProdRevision() {
//		def targetSytemFileName = config.target.system.mapping.file.name
//		def configDir = config.config.dir
//		def targetSystemFile = new File("${configDir}/${targetSytemFileName}")
//		def jsonSystemTargets = new JsonSlurper().parseText(targetSystemFile.text)
//		def prodTarget
//		jsonSystemTargets.targetSystems.each{ target ->
//			if(target.name.equalsIgnoreCase("produktion")) {
//				prodTarget = target.target
//			}
//		}
//		def revisions = new JsonSlurper().parseText(new File(config.revision.file.path).text)
//		def prodRev = revisions.lastRevisions[prodTarget]
//		
//		// JHE (15.06.2018) : we print the json on stdout so that the pipeline can get and parse it. Unfortunately there is currently no supported alternative: https://issues.jenkins-ci.org/browse/JENKINS-26133
//		def json = JsonOutput.toJson([lastProdRevision:prodRev])
//		println json
//	}
//	
//	def saveRevisions(def targetInd, def installationTarget, def revision) {
//		
//		println("(saveRevisions) Saving revisions for targetInd=${targetInd}, installationTarget=${installationTarget}, revision=${revision}")
//		
//		def revisionFile = new File(config.revision.file.path)
//		def currentRevision = [P:1,T:10000]
//		def lastRevision = [:]
//		def revisions = [lastRevisions:lastRevision, currentRevision:currentRevision]
//		if (revisionFile.exists()) {
//			revisions = new JsonSlurper().parseText(revisionFile.text)
//			println("(saveRevisions) Current revisions are: ${revisions}")
//		}
//		if(targetInd.equals("P")) {
//			println("(saveRevisions) Increasing Prod revision ...")
//			revisions.currentRevision[targetInd]++
//		}
//		else {
//			// We increase it only when saving a new Target
//			if(revisions.lastRevisions.get(installationTarget) == null) {
//				revisions.currentRevision[targetInd] = revisions.currentRevision[targetInd] + 10000
//				println("(saveRevisions) ${installationTarget} was new, next test range revision will start at ${revisions.currentRevision[targetInd]}")
//			}
//		}
//		revisions.lastRevisions[installationTarget] = revision
//		println("(saveRevisions) Following revisions will be save: ${revisions}")
//		new File(config.revision.file.path).write(new JsonBuilder(revisions).toPrettyString())
//		
//	}
//	
//	def resetLastRevision(def target) {
//		
//		def revisions = getParsedRevisionFile()
//		
//		// JHE (26.07.2018): If revisions doesn't contain the target, it means that nothing has ever been patch on the target -> then we don't have to do anything.
//		// JHE (13.08.2018): If lastRevision ends with "@P", it means nothing has been patched since last clone -> then we don't have to do anything.
//		if(revisions.lastRevisions[target] != null && !revisions.lastRevisions[target].toString().endsWith("@P")) {
//			def rangeStep = config.revision.range.step
//			def prodTarget = getProdTarget()
//			
//			println("Resetting last revision for ${target}")
//			println("Current revisions are: ${revisions}")
//			println("Prod target = ${prodTarget}")
//			
//			// For the cloned target, we reset its version (eg.: 10045 will be 10000), and we add the @P indicator
//			def initialRevision = ((int) (revisions.lastRevisions[target].toInteger() / rangeStep)) * rangeStep
//			revisions.lastRevisions[target] = initialRevision + "@P"
//			
//			println("Following revisions will be written to ${config.revision.file.path} : ${revisions}")
//			
//			new File(config.revision.file.path).write(new JsonBuilder(revisions).toPrettyString())
//		}
//		else {
//			println("Nothing to reset for ${target} within Revisions.json. We probably never have any patch installed directly on ${target}.")
//		}
//	}
//	
//	private Object getParsedRevisionFile() {
//		File revisionFile = new File(config.revision.file.path)
//		def revisions = [:]
//		
//		if (revisionFile.exists()) {
//			revisions = new JsonSlurper().parseText(revisionFile.text)
//		}
//		else {
//			throw new RuntimeException("Error while parsing Revision file. ${revisionFile} not found.")
//		}
//		
//		return revisions
//	}
//	
//	private String getProdTarget() {
//		def targetSystemFileName = config.target.system.mapping.file.name
//		def configDir = config.config.dir
//		def targetSystemFile = new File("${configDir}/${targetSystemFileName}")
//		def targetSystems = [:]
//		
//		if (targetSystemFile.exists()) {
//			targetSystems = new JsonSlurper().parseText(targetSystemFile.text)
//		}
//				
//		def prodTarget = ""
//				
//		targetSystems.targetSystems.each{targetSystem ->
//			if(targetSystem.typeInd.equalsIgnoreCase("P")) {
//				prodTarget = targetSystem.target
//			}
//		}
//				
//		return prodTarget
//	}
//	
//	def getLastRevisionForTarget(String target) {
//		
//		def revisions = getParsedRevisionFile()
//		
//		def lastRevisionForTarget = revisions.lastRevisions[target]
//		
//		if (lastRevisionForTarget != null) {
//			return lastRevisionForTarget
//		}
//		else {
//			return null
//		}
//	}
}
