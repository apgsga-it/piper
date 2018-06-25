package com.apgsga.patch.service.client

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class PatchCloneClient {
	
	private final String FIRST_PART_FOR_ARTIFACT_SEARCH = "T-"
	
	private final int rangeStep = 10000
	
	public void onClone(def target, def dryRun, def revisionFilePath, def targetSystemFilePath) {
		
		println("Starting clone process for ${target}.")
		
		Long lastRevision = getLastRevisionForTarget(target,revisionFilePath)
		
		println("Last revision for ${target} was ${lastRevision}")
		
		// If the target doesn't exit, we don't have anything to delete, and don't have any revision to reset. It simply means that so far no patch has been installed on this target.
		if(lastRevision != null) {
			deleteRevisionWithinRange(lastRevision,dryRun)
			resetLastRevision(target,revisionFilePath,targetSystemFilePath)
		}
	}
	
	private Long getLastRevisionForTarget(String target, def revisionFilePath) {
		
		def revisions = getParsedRevisionFile(revisionFilePath)
		
		def lastRevisionForTarget = revisions.lastRevisions[target]
		
		return Long.valueOf(lastRevisionForTarget)	
	}
	
	private void deleteRevisionWithinRange(Long lastRevision, def dryRun) {
		
		def from = ((int) (lastRevision / rangeStep)) * rangeStep
		
		println("Artifact from ${from} to ${lastRevision} will be deleted from Artifactory.")
		
		PatchArtifactoryClient patchArtifactoryClient = new PatchArtifactoryClient()
		
		// TODO JHE: We can probably improve (remove) this loop by using a more sophisticated Regex
		while(from <= lastRevision) {
			// TODO JHE: do we want to search in more repos? Is "realeases" enough?
			patchArtifactoryClient.removeArtifacts("*${FIRST_PART_FOR_ARTIFACT_SEARCH}${from}*", dryRun)
			from++
		}
	}
	
	private void resetLastRevision(def target, def revisionFilePath, def targetSystemFilePath) {
		
		def revisions = getParsedRevisionFile(revisionFilePath)
		
		def prodTarget = getProdTarget(targetSystemFilePath)
		
		println("Resetting last revision for ${target}")
		println("Current revisions are: ${revisions}")
		println("Prod target = ${prodTarget}")
		
		// For the cloned target, we reset its version (eg.: 10045 will be 10000), and we add the @P indicator
		def initialRevision = ((int) (revisions.lastRevisions[target].toInteger() / rangeStep)) * rangeStep
		revisions.lastRevisions[target] = initialRevision + "@P"
		
		println("Following revisions will be written to ${revisionFilePath} : ${revisions}")
		
		new File(revisionFilePath).write(new JsonBuilder(revisions).toPrettyString())
	}
	
	private String getProdTarget(def targetSystemFilePath) {
		File targetSystemFileName = new File(targetSystemFilePath)
		def targetSystems = [:]
		
		if (targetSystemFileName.exists()) {
			targetSystems = new JsonSlurper().parseText(targetSystemFileName.text)
		}
				
		def prodTarget = ""
				
		targetSystems.targetSystems.each{targetSystem ->
			if(targetSystem.typeInd.equalsIgnoreCase("P")) {
				prodTarget = targetSystem.target
			}
		}
				
		return prodTarget
	}
	
	private Object getParsedRevisionFile(String revisionFilePath) {
		File revisionFile = new File(revisionFilePath)
		def revisions = [:]
		
		if (revisionFile.exists()) {
			revisions = new JsonSlurper().parseText(revisionFile.text)
		}
		else {
			throw new RuntimeException("Error while parsing Revision file. ${revisionFile} not found.")
		}
		
		return revisions
	}

}
