package com.apgsga.patch.service.client

import org.springframework.boot.test.context.SpringBootTest
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.jfrog.artifactory.client.Artifactory
import org.jfrog.artifactory.client.ArtifactoryClientBuilder
import org.jfrog.artifactory.client.model.RepoPath
import spock.lang.Specification

class PatchCloneClientTest extends Specification {
	
	def testRevisionFilePath = "src/test/resources/Revisions.json"
	
	def testTargetSystemFilePath = "src/test/resources/config/TargetSystemMappings.json"
	
	def "Test onClone method for a non-valid target, shouldn't get an excpetion"() {
		setup:
			def client = new PatchCloneClient(testRevisionFilePath,testTargetSystemFilePath)
		when:
			client.onClone("NON_TARGET")
		then:
			// Simply nothing should happen.
			notThrown(RuntimeException)
	}
	
	def "Test onClone method, validate that Artifact are effectively deleted from Artifactory and that revision has been reset"() {
		setup:
			def url = "https://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga"
			def user = "dev"
			def password = "dev1234"
			def artifactory = ArtifactoryClientBuilder.create().setUrl(url).setUsername(user).setPassword(password).build()
			def client = new PatchCloneClient(testRevisionFilePath,testTargetSystemFilePath)
			def jarFile1 = new File("src/test/resources/forArtifactoryTest/patchCloneTest-T-10001.jar")
			artifactory.repository("releases").upload("patchCloneTest-T-10001", jarFile1).doUpload()
			println "${jarFile1} uploaded!"
			def jarFile2 = new File("src/test/resources/forArtifactoryTest/patchCloneTest-T-10002.jar")
			println "${jarFile2} uploaded!"
			artifactory.repository("releases").upload("patchCloneTest-T-10001", jarFile2).doUpload()
			def jarFile3 = new File("src/test/resources/forArtifactoryTest/patchCloneTest-T-10015.jar")
			println "${jarFile3} uploaded!"
			artifactory.repository("releases").upload("patchCloneTest-T-10001", jarFile3).doUpload()
			
			def oriRevisionFileName = "src/test/resources/Revisions.json"
			File oriRevisionFile = new File(oriRevisionFileName)
			def revisionsBackup = [:]
			if (oriRevisionFile.exists()) {
				revisionsBackup = new JsonSlurper().parseText(oriRevisionFile.text)
			}
			
			[
				"patchCloneTest-T-10001",
				"patchCloneTest-T-10001",
				"patchCloneTest-T-10001"
			].each { jar ->
				List<RepoPath> searchItems = artifactory.searches()
						.repositories("releases")
						.artifactsByName(jar)
						.doSearch();
				assert (searchItems.size() == 1) : "Error when uploading ${jar} for testing purpose."
			}
		when:
			client.onClone("CHEI212")
		then:
			notThrown(RuntimeException)
			
			[
				"patchCloneTest-T-10001",
				"patchCloneTest-T-10001",
				"patchCloneTest-T-10001"
			].each { jar ->
				List<RepoPath> searchItems = artifactory.searches()
						.repositories("releases")
						.artifactsByName(jar)
						.doSearch();
				assert (searchItems.size() == 0) : "${jar} has not been deleted !"
			}
			
			def revisionFileName = "src/test/resources/Revisions.json"
			File revisionFile = new File(revisionFileName)
			def revisions = [:]
			if (revisionFile.exists()) {
				revisions = new JsonSlurper().parseText(revisionFile.text)
			}
			def currentChei212Revision = revisions.lastRevisions["CHEI212"]
			def currentProdRevision = revisions.lastRevisions["CHEI211"]
			if(currentProdRevision != null) {
				assert (currentChei212Revision == currentProdRevision) : "Revision has not been reset correctly. CHEI212 is ${currentChei212Revision} and PROD is ${currentProdRevision}"
			}
			else {
				assert (currentChei212Revision.equals("SNAPSHOT")) : "Revision for CHEI212 should have been set to SNAPSHOT as no revision exists for PROD yet."
			}
			
			new File(revisionFileName).write(new JsonBuilder(revisionsBackup).toPrettyString())
			
	}
	
}
