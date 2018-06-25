package com.apgsga.patch.service.client

import org.jfrog.artifactory.client.Artifactory
import org.jfrog.artifactory.client.model.RepoPath
import org.jfrog.artifactory.client.ArtifactoryClientBuilder

class PatchArtifactoryClient {
	
	private Artifactory artifactory;
	
	private final String RELEASE_REPO = "releases"
	
	private final String DB_PATCH_REPO = "dbpatch"
	
	def PatchArtifactoryClient() {
		// JHE (25.06.2018): apscli.properties was ainly thought to store properties which are different for test and prod. Do we want the 3 below also there? Or rather in a property file which will not be packaged, but accessible from outside?
		def url = "https://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga"
		def user = "dev"
		def password = "dev1234"
		artifactory = ArtifactoryClientBuilder.create().setUrl(url).setUsername(user).setPassword(password).build();
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
	
	public def deleteAllTRevisions(def options, def revisionFilePath, def patchRevisionClient) {
		println "Removing all T Artifact from Artifactory."
		boolean dryRun = true
		if(options.rtr.size() > 0) {
			if(options.rtr[0] == "0") {
				dryRun = false
			}
		}
		
		removeArtifacts("*-T-*", dryRun);
		if(!dryRun) {
			new File(revisionFilePath).delete()
		}
	}
	
}
