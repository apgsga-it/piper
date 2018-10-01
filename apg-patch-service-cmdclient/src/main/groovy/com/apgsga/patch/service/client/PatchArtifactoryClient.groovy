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
	
	private final String FIRST_PART_FOR_ARTIFACT_SEARCH = "T-"
	
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
				println "${repoPath} removed!"
			}
		};
	}
	
	// TODO JHE (26.06.2018): will be remove with JAVA8MIG-389 
	// TODO JHE: To be deleted -> we don't have "T" releases anymore !!!
	/*
	public def deleteAllTRevisions(def dryRun) {
		println "Removing all T Artifact from Artifactory."
		removeArtifacts("*-T-*", dryRun);
		if(!dryRun) {
			new File(config.revision.file.path).delete()
		}
	}
	*/
	
	def cleanReleases(def target) {
		
		def cmd = "/opt/apg-patch-cli/bin/apsrevcli.sh -llr ${target}"
		def revision = sh ( returnStdout : true, script: cmd).trim()
		
		if(revision != '') {
			println "Following revision would have been deleted"
			revision.each {
				println("rev ${it}")
			}
		}
		
		/*
		def PatchRevisionClient patchRevisionClient = new PatchRevisionClient(config)
		def revisionsToBeDeleted = patchRevisionClient.getInstalledRevisions(target)
		// JHE (26.07.2018): If lastRevision is null, it means that nothing has ever been patch on the target -> then we don't have to do anything.
		// JHE (13.08.2018): If lastRevision ends with "@P", it means nothing has been patched since last clone -> then we don't have to do anything.
		if(lastRevision != 'SNAPSHOT') {
		 
			def rangeStep = config.revision.range.step
			def from = ((int) (Long.valueOf(lastRevision) / rangeStep)) * rangeStep
			def dryRun = config.onclone.delete.artifact.dryrun
			def originalFrom = from
			
			println("Artifact from ${from} to ${lastRevision} will be deleted from Artifactory.")
			
			// TODO JHE: We can probably improve (remove) this loop by using a more sophisticated Regex
			while(from <= Long.valueOf(lastRevision)) {
				// TODO JHE: do we want to search in more repos? Is "realeases" enough?
				removeArtifacts("*${FIRST_PART_FOR_ARTIFACT_SEARCH}${from}*", dryRun)
				from++
			}
			
			// Cleaning Docker Image as well
			if(!dryRun) {
				def jadasCleanupCmd = "/opt/apgops/cleanup_jadas_images.sh ${originalFrom} ${lastRevision}"
				println "Following command will be started to clean Jadas images : ${jadasCleanupCmd}"
				['bash', '-c', jadasCleanupCmd].execute().getOutputStream().println()
				println "Jadas Images have been deleted for ${target}"
			}
			
		}
		*/
		else {
			println("No release to clean for ${target}. We probably never have any patch installed directly on ${target}, or no patch has been newly installed since last clone.")
		}
	}
}
