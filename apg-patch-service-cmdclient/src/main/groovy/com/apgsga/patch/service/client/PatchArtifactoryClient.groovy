package com.apgsga.patch.service.client

import org.jfrog.artifactory.client.Artifactory
import org.jfrog.artifactory.client.model.RepoPath

import com.apgsga.patch.service.client.revision.PatchRevisionClient

import org.jfrog.artifactory.client.ArtifactoryClientBuilder

class PatchArtifactoryClient {
	
	private def config
	
	private Artifactory artifactory;
	
	private final String RELEASE_REPO = "releases"
	
	private final String DB_PATCH_REPO = "dbpatch"
	
	def PatchArtifactoryClient(def configuration) {
		config = configuration
		def pass = System.getenv('REPO_RO_PASSWD')
		assert pass != null
		artifactory = ArtifactoryClientBuilder.create().setUrl(config.artifactory.url).setUsername(config.artifactory.user).setPassword(pass).build();
	}
	
	public def removeArtifacts(String regex, boolean dryRun) {
		List<RepoPath> searchItems = artifactory.searches().repositories(RELEASE_REPO,DB_PATCH_REPO).artifactsByName(regex).doSearch();
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
		def revAsListToCleanJadas = ""
		def jadasCleanupCmd = "/opt/apgops/cleanup_jadas_images_by_revision_DEV.sh"
		def revisionAsString
		
		if(revision != null) {
			revision.each {
				// Will delete all published JAR, POM, ZIP, etc ... for the given version/revision
				removeArtifacts("*-${it}.*", dryRun)
				// Will delete all published sources jar for the given version/revision
				removeArtifacts("*-${it}-sources.jar", dryRun)
				
				revisionAsString = it.toString()
				revisionAsString = revisionAsString.substring(revisionAsString.substring(revisionAsString.lastIndexOf("-")+1, revisionAsString.length()))
				revAsListToCleanJadas = revAsListToCleanJadas + " " + revisionAsString
			}
			
			if(!dryRun) {
				println "Executing: ${jadasCleanupCmd}${revAsListToCleanJadas}"
				['bash', '-c', "${jadasCleanupCmd}${revAsListToCleanJadas}"].execute().in.text
			}
			else {
				println "Following script would have been called to clean Jadas images: ${jadasCleanupCmd}${revAsListToCleanJadas}"
			}
		}			
		else {
			println("No release to clean for ${target}. We probably never have any patch installed directly on ${target}, or no patch has been newly installed since last clone.")
		}
	}
}
