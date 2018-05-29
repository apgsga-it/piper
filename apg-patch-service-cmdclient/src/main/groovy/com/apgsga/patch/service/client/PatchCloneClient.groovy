package com.apgsga.patch.service.client

import java.util.List
import org.jfrog.artifactory.client.Artifactory
import org.jfrog.artifactory.client.ArtifactoryClientBuilder
import org.jfrog.artifactory.client.model.RepoPath
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class PatchCloneClient {
	
	private final String FIRST_PART_FOR_ARTIFACT_SEARCH = "T-"
	
	private final String RELEASE_REPO = "releases"
	
	private Artifactory artifactory;
	
	private revisionFilePath
	
	private targetSystemFilePath
	
	//TODO JHE: As soon as JAVA8MIG-363 will be done, p_revisionFilePatch will probably be removed
	//TODO JHE: As soon as JAVA8MIG-363 will be done, we'll probably also have a property within cmdCli configuration file which will contain path to targetSystemFilePath
	public PatchCloneClient(String p_revisionFilePath, String p_targetSystemFilePath) {
		// TODO JHE: As soon as JAVA8MIG-363 will be done -> read properties from /etc/opt/apg-patch-cli.
		//			 										 name of the file is still to be determined.
		def url = "https://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga"
		def user = "dev"
		def password = "dev1234"
		artifactory = ArtifactoryClientBuilder.create().setUrl(url).setUsername(user).setPassword(password).build();
		revisionFilePath = p_revisionFilePath
		targetSystemFilePath = p_targetSystemFilePath
	}
	
	public void onClone(String target) {
		Long lastRevision = getLastRevisionForTarget(target)
		
		// If the target doesn't exit, we don't have anything to delete, and don't have any revision to reset. It simply means that so far no patch has been installed on this target.
		if(lastRevision != null) {
			deleteRevisionWithinRange(lastRevision)
			resetLastRevision(target)
		}
	}
	
	private Long getLastRevisionForTarget(String target) {
		
		def revisions = getParsedRevisionFile()
		
		def lastRevisionForTarget = revisions.lastRevisions[target]
		
		return lastRevisionForTarget	
	}
	
	private void deleteRevisionWithinRange(Long lastRevision) {
		
		// TODO JHE: Ideally the range step should be centralized somewhere
		//			 Might be improved when/if we decide to implement JAVA8MIG-365.
		def rangeStep = 10000
		def from = ((int) (lastRevision / rangeStep)) * rangeStep
		
		// TODO JHE: We can probably improve (remove) this loop by using a more sophisticated Regex
		while(from <= lastRevision) {
			// TODO JHE: do we want to search in more repos? Is "realeases" enough?
			removeArtifacts(RELEASE_REPO, "*${FIRST_PART_FOR_ARTIFACT_SEARCH}${from}*", false)
			from++
		}
	}
	
	private void resetLastRevision(String target) {
		
		def revisions = getParsedRevisionFile()
		
		def prodTarget = getProdTarget()
		
		// TODO JHE: Is this part really correct? I mean that we eventually set it to SNAPSHOT? Not sure it will even happen, but ...
		def currentProdRev = revisions.lastRevisions[prodTarget]
		if(currentProdRev == null) {
			currentProdRev = "SNAPSHOT"
		}
		
		revisions.lastRevisions[target] = currentProdRev
		new File(revisionFilePath).write(new JsonBuilder(revisions).toPrettyString())
	}
	

	private String getProdTarget() {
		File targetSystemFileName = new File(targetSystemFilePath)
		def targetSystems = [:]
		
		if (targetSystemFileName.exists()) {
			targetSystems = new JsonSlurper().parseText(targetSystemFileName.text)
		}
				
		def prodTarget = ""
				
		targetSystems.targetSystems.each{targetSystem ->
			if(targetSystem.name.equalsIgnoreCase("Produktion")) {
				prodTarget = targetSystem.target
			}
		}
				
		return prodTarget
	}
	
	private Object getParsedRevisionFile() {
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
	
	private void removeArtifacts(String repo, String regex, boolean dryRun) {
		List<RepoPath> searchItems = artifactory.searches().repositories(repo).artifactsByName(regex).doSearch();
		searchItems.each{repoPath ->
			if(dryRun) {
				println "${repoPath} would have been deleted."
			}
			else {
				artifactory.repository(repo).delete(repoPath.getItemPath());
			}
		};
	}
}
