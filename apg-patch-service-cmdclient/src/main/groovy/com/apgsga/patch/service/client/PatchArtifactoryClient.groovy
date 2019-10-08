package com.apgsga.patch.service.client

import org.jfrog.artifactory.client.Artifactory
import org.jfrog.artifactory.client.model.RepoPath

import com.apgsga.patch.service.client.revision.PatchRevisionClient

import org.jfrog.artifactory.client.ArtifactoryClientBuilder

class PatchArtifactoryClient {
	
	private def config
	
	private Artifactory artifactory;
	
	private final String RELEASE_REPO
	
	private final String DB_PATCH_REPO
	
	private final String RPM_PATCH_REPO
	
	def PatchArtifactoryClient(def configuration) {
		config = configuration
		def pass = System.getenv('REPO_RO_PASSWD')
		assert pass != null
		artifactory = ArtifactoryClientBuilder.create().setUrl(config.mavenrepo.baseurl).setUsername(config.mavenrepo.user.name).setPassword(pass).build();
		RELEASE_REPO = config.artifactory.release.repo.name
		DB_PATCH_REPO = config.artifactory.dbpatch.repo.name
		RPM_PATCH_REPO = config.artifactory.patch.rpm.repo.name
	}
	
	private def removeArtifacts(String regex, boolean dryRun, Iterable<String> repos) {

		List<RepoPath> searchItems = artifactory.searches().repositories(repos.join(",")).artifactsByName(regex).doSearch();
		searchItems.each{repoPath ->
			if(dryRun) {
				println "${repoPath} would have been deleted."
			}
			else {
				artifactory.repository(repoPath.getRepoKey()).delete(repoPath.getItemPath());
				println "${repoPath} removed!"
			}
		};
	}
	
	def cleanReleases(def target) {
		
		def revisionClient = new PatchRevisionClient(config)
		def revision = revisionClient.getInstalledRevisions(target)
		def dryRun = config.onclone.delete.artifact.dryrun
		
		println "Following revisions have been found for ${target}: ${revision}"
		
		if(revision != null) {
			revision.each {
				def revisionFormatedForSearch = "${it}".substring("${it}".lastIndexOf("-"),"${it}".length())
				println "Starting to clean Artifact for revision ${revision} (revisionFormatedForSearch = ${revisionFormatedForSearch})"
				removeArtifacts("*${revisionFormatedForSearch}.*", dryRun, [RPM_PATCH_REPO,RELEASE_REPO,DB_PATCH_REPO])
				println "Done cleaning Artifacts for ${revision}."
			}
		}			
		else {
			println("No release to clean for ${target}. We probably never have any patch installed directly on ${target}, or no patch has been newly installed since last clone.")
		}
	}
}