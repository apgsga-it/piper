package com.apgsga.patch.service.client

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

class PatchRevisionClient {
	
	def retrieveRevisions(def options, def revisionFilePath) {
		
		// TODO JHE: move to PatchRevisionClient

		def targetInd = options.rrs[0]
		def installationTarget = options.rrs[1]
		
		println("(retrieveRevisions) Retrieving revision for target ${installationTarget} (${targetInd})")

		def revisionFile = new File(revisionFilePath)
		def currentRevision = [P:1,T:10000]
		def lastRevision = [:]
		def revisions = [lastRevisions:lastRevision, currentRevision:currentRevision]
		def patchRevision
		def patchLastRevision
		if (revisionFile.exists()) {
			revisions = new JsonSlurper().parseText(revisionFile.text)
			println("(retrieveRevisions) Revision file exist and following content has been parsed: ${revisions}")
		}

		if(targetInd.equals("P")) {
			patchRevision = revisions.currentRevision[targetInd]
			println("(retrieveRevisions) targetInd = P ... patchRevision = ${patchRevision}")
		}
		else {
			if(revisions.lastRevisions.get(installationTarget) == null) {
				patchRevision = revisions.currentRevision[targetInd]
				println("(retrieveRevisions) Revision for ${installationTarget} was null, patchRevision will be ${patchRevision}")
			}
			else {
				def currentLastRevision = revisions.lastRevisions.get(installationTarget)
				println("(retrieveRevisions) currentLastRevision = ${currentLastRevision}")
				if(currentLastRevision.endsWith("@P")) {
					patchRevision = currentLastRevision.substring(0, currentLastRevision.size()-2)
					patchLastRevision = "CLONED"
					println("(retrieveRevisions) New patch for ${installationTarget} after clone")
					println("(retrieveRevisions) patchRevision = ${patchRevision} / patchLastRevision = ${patchLastRevision}")
				}
				else {
					patchRevision = (revisions.lastRevisions.get(installationTarget)).toInteger() + 1
					println("(retrieveRevisions) patchRevision = ${patchRevision}")
				}
			}
		}
		
		if(patchLastRevision == null) {
			patchLastRevision = revisions.lastRevisions.get(installationTarget,'SNAPSHOT')
		}

		println("(retrieveRevisions) patchLastRevision = ${patchLastRevision}")
		// JHE (31.05.2018) : we print the json on stdout so that the pipeline can get and parse it. Unfortunately there is currently no supported alternative: https://issues.jenkins-ci.org/browse/JENKINS-26133
		def json = JsonOutput.toJson([fromRetrieveRevision:[revision: patchRevision, lastRevision: patchLastRevision]])
		println("(retrieveRevisions) Following json will be returned : ${json}")
		println json
	}
	
	def retrieveLastProdRevision(def targetSystemMappingsFilePath, def revisionFilePath) {
		def targetSystemFile = new File(targetSystemMappingsFilePath)
		def jsonSystemTargets = new JsonSlurper().parseText(targetSystemFile.text)
		def prodTarget
		jsonSystemTargets.targetSystems.each{ target ->
			if(target.typeInd == "P") {
				prodTarget = target.target
			}
		}
		def revisions = new JsonSlurper().parseText(new File(revisionFilePath).text)
		def prodRev = revisions.lastRevisions[prodTarget]
		
		// JHE (15.06.2018) : we print the json on stdout so that the pipeline can get and parse it. Unfortunately there is currently no supported alternative: https://issues.jenkins-ci.org/browse/JENKINS-26133
		def json = JsonOutput.toJson([lastProdRevision:prodRev])
		println json
	}
	
	def saveRevisions(def options, def revisionFilePath) {
		
		def targetInd = options.srs[0]
		def installationTarget = options.srs[1]
		def revision = options.srs[2]
				
		println("(saveRevisions) Saving revisions for targetInd=${targetInd}, installationTarget=${installationTarget}, revision=${revision}")
		
		def revisionFile = new File(revisionFilePath)
		def currentRevision = [P:1,T:10000]
		def lastRevision = [:]
		def revisions = [lastRevisions:lastRevision, currentRevision:currentRevision]
		if (revisionFile.exists()) {
			revisions = new JsonSlurper().parseText(revisionFile.text)
			println("(saveRevisions) Current revisions are: ${revisions}")
		}
		if(targetInd.equals("P")) {
			println("(saveRevisions) Increasing Prod revision ...")
			revisions.currentRevision[targetInd]++
		}
		else {
			// We increase it only when saving a new Target
			if(revisions.lastRevisions.get(installationTarget) == null) {
				revisions.currentRevision[targetInd] = revisions.currentRevision[targetInd] + 10000
				println("(saveRevisions) ${installationTarget} was new, next test range revision will start at ${revisions.currentRevision[targetInd]}")
			}
		}
		revisions.lastRevisions[installationTarget] = revision
		println("(saveRevisions) Following revisions will be save: ${revisions}")
		new File(revisionFilePath).write(new JsonBuilder(revisions).toPrettyString())
		
	}
	
	def removeAllTRevisions(def options, def revisionFilePath, def targetSystemMappingFilePath) {
		println "Removing all T Artifact from Artifactory."
		boolean dryRun = true
		if(options.rtr.size() > 0) {
			if(options.rtr[0] == "0") {
				dryRun = false
			}
		}
		def cloneClient = new PatchCloneClient(revisionFilePath,targetSystemMappingFilePath)
		cloneClient.deleteAllTRevisions(dryRun)
	}
	
	def renameRevisionFile(def revisionFilePath) {
		def revisionFile = new File(revisionFilePath)
		def fileSuffix = new Date().format("yyyyMMdd_HHmmss")
		revisionFile.renameTo(revisionFilePath + "." + fileSuffix)
	}
}
