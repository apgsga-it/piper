package com.apgsga.patch.service.client

import java.util.List
import org.jfrog.artifactory.client.Artifactory
import org.jfrog.artifactory.client.ArtifactoryClientBuilder
import org.jfrog.artifactory.client.model.RepoPath
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class PatchCloneClient {
	
	private final String PROD_TARGET = "CHPI211"
	
	private final String FIRST_PART_FOR_ARTIFACT_SEARCH = "T-"
	
	private final String RELEASE_REPO = "releases"
	
	private Artifactory artifactory;
	
	private revisionFilePath
	
	public PatchCloneClient(String p_revisionFilePath) {
		// TODO JHE: Get these from a configuration file. Maybe a new file: /var/opt/apg-patch-common/artifactory.properties ?? 
		def url = "https://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga"
		def user = "dev"
		def password = "dev1234"
		artifactory = ArtifactoryClientBuilder.create().setUrl(url).setUsername(user).setPassword(password).build();
		revisionFilePath = p_revisionFilePath
	}
	
	public void onClone(String target) {
		Long lastRevision = getLastRevisionForTarget(target)
		deleteRevisionWithinRange(lastRevision)
		resetLastRevision(target)
	}
	
	private Long getLastRevisionForTarget(String target) {
		
		def revisions = getParsedRevisionFile()
		
		def lastRevisionForTarget = revisions.lastRevisions[target]
		if(lastRevisionForTarget == null) {
			throw new RuntimeException("No revision found for ${target}.")
		}		
		
		return lastRevisionForTarget	
	}
	
	private void deleteRevisionWithinRange(Long lastRevision) {
		
		// TODO JHE: Ideally the range step should be centralized somewhere
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
		
		// TODO JHE: Is this part really correct? I mean that we eventually set it to SNAPSHOT? Not sure it will even happen, but ...
		def currentProdRev = revisions.lastRevisions[PROD_TARGET]
		if(currentProdRev == null) {
			currentProdRev = "SNAPSHOT"
		}
		
		revisions.lastRevisions[target] = currentProdRev
		new File(revisionFilePath).write(new JsonBuilder(revisions).toPrettyString())
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
	
	private void listArtifacts(String repoId, String regex) {
		List<RepoPath> searchItems = artifactory.searches().repositories(repoId).artifactsByName(regex).doSearch();
		System.out.println("Listing all Artifacts for repo " + repoId);
		searchItems.each{repoPath -> 
			System.out.println(repoPath.getItemPath());
		};
		System.out.println("Total number of Artifacts in " + repoId + ": " + searchItems.size());
		
	}
	
	private void removeArtifacts(String repo, String regex, boolean dryRun) {
		if(dryRun) {
			println "Dry run only ... following Artifacts would have been deleted";
			listArtifacts(repo,regex);
		}
		else {
			List<RepoPath> searchItems = artifactory.searches().repositories(repo).artifactsByName(regex).doSearch();
			searchItems.each{repoPath ->
				artifactory.repository(repo).delete(repoPath.getItemPath());
			};
		}
	}

}
