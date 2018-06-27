package com.apgsga.patch.service.client

import org.jfrog.artifactory.client.Artifactory
import org.jfrog.artifactory.client.model.RepoPath
import org.jfrog.artifactory.client.ArtifactoryClientBuilder

class PatchArtifactoryClient {
	
	private def config
	
	private Artifactory artifactory;
	
	private final String RELEASE_REPO = "releases"
	
	private final String DB_PATCH_REPO = "dbpatch"
	
	def PatchArtifactoryClient(def configuration) {
		config = configuration
		artifactory = ArtifactoryClientBuilder.create().setUrl(config.artifactory.url).setUsername(config.artifactory.user).setPassword(config.artifactory.password).build();
	}
	
	public def removeArtifacts(String regex, boolean dryRun) {
		List<RepoPath> searchItems = artifactory.searches().repositories(RELEASE_REPO,DB_PATCH_REPO).artifactsByName(regex).doSearch();
		searchItems.each{repoPath ->
			if(dryRun) {
				println "${repoPath} would have been deleted."
			}
			else {
				artifactory.repository(repoPath.getRepoKey()).delete(repoPath.getItemPath());
			}
		};
	}
	
	// TODO JHE (26.06.2018): will be remove with JAVA8MIG-389 
	public def deleteAllTRevisions(def dryRun) {
		println "Removing all T Artifact from Artifactory."
		removeArtifacts("*-T-*", dryRun);
		if(!dryRun) {
			new File(config.revision.file.path).delete()
		}
	}
	
}
