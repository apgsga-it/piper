package com.apgsga.patch.service.revision

import org.springframework.boot.test.context.SpringBootTest

import com.apgsga.patch.service.client.PatchCloneClient

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.jfrog.artifactory.client.Artifactory
import org.jfrog.artifactory.client.ArtifactoryClientBuilder
import org.jfrog.artifactory.client.model.RepoPath
import spock.lang.Specification

class DeleteAllTRevisionsTest extends Specification {
	
	def testRevisionFilePath = "src/test/resources/Revisions.json"
	
	def testTargetSystemFilePath = "src/test/resources/config/TargetSystemMappings.json"
	
	def "Test delete all T revision with dryRun"() {
		setup:
			def client = new PatchCloneClient(testRevisionFilePath,testTargetSystemFilePath)
		when:
			client.deleteAllTRevisions(true)
		then:
			// Simply nothing should happen.
			notThrown(RuntimeException)
	}
}

