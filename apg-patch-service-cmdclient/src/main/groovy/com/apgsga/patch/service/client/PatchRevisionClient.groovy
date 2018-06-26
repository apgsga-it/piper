package com.apgsga.patch.service.client

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

class PatchRevisionClient {
	
	private config
	
	public PatchRevisionClient(def configuration) {
		config = configuration
	}
	
	def retrieveRevisions(def targetInd, def installationTarget) {
		
		println("(retrieveRevisions) Retrieving revision for target ${installationTarget} (${targetInd})")

		def revisionFile = new File(config.revision.file.path)
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
		println json
	}
	
	def retrieveLastProdRevision() {
		def targetSytemFileName = config.target.system.mapping.file.name
		def configDir = config.config.dir
		def targetSystemFile = new File("${configDir}/${targetSytemFileName}")
		def jsonSystemTargets = new JsonSlurper().parseText(targetSystemFile.text)
		def prodTarget
		jsonSystemTargets.targetSystems.each{ target ->
			if(target.typeInd == "P") {
				prodTarget = target.target
			}
		}
		def revisions = new JsonSlurper().parseText(new File(config.revision.file.path).text)
		def prodRev = revisions.lastRevisions[prodTarget]
		
		// JHE (15.06.2018) : we print the json on stdout so that the pipeline can get and parse it. Unfortunately there is currently no supported alternative: https://issues.jenkins-ci.org/browse/JENKINS-26133
		def json = JsonOutput.toJson([lastProdRevision:prodRev])
		println json
	}
	
	def saveRevisions(def targetInd, def installationTarget, def revision) {
		
		println("(saveRevisions) Saving revisions for targetInd=${targetInd}, installationTarget=${installationTarget}, revision=${revision}")
		
		def revisionFile = new File(config.revision.file.path)
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
		new File(config.revision.file.path).write(new JsonBuilder(revisions).toPrettyString())
		
	}
}
