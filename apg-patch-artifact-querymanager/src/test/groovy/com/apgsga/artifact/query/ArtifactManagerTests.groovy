package com.apgsga.artifact.query
import java.lang.ClassLoader.ParallelLoaders
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.springframework.core.io.FileSystemResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.util.FileSystemUtils

import com.apgsga.microservice.patch.api.SearchCondition
import com.google.common.collect.Count

import spock.lang.Specification;

class ArtifactManagerTests extends Specification {

	def "Default Filter Selection of Artifacts"() {
		setup:
		def artifactManager = ArtifactManager.create("com.affichage.common.maven","dm-bom","target/maverepo")
		when:
		def results = artifactManager.getAllDependencies("9.0.6.ADMIN-UIMIG-SNAPSHOT")
		def nonApgResults = results.findAll{ (!it.groupId.startsWith("com.apgsga") && ! it.groupId.startsWith("com.affichage"))}
		then:
		assert results.size() > 0
		assert nonApgResults.size() == 0
	}



	def "With All Filter Selection of Artifacts"() {
		setup:
		def artifactManager = ArtifactManager.create("com.affichage.common.maven","dm-bom","target/maverepo")
		when:
		def results = artifactManager.getAllDependencies("9.0.6.ADMIN-UIMIG-SNAPSHOT",SearchCondition.ALL)
		println results.size()
		def nonApplicationResults = results.findAll{ (!it.groupId.startsWith("com.apgsga") && ! it.groupId.startsWith("com.affichage"))}
		def applicationResults = results.findAll{ (it.groupId.startsWith("com.apgsga") ||  it.groupId.startsWith("com.affichage"))}

		then:
		assert results.size() > 0
		assert nonApplicationResults.size() > 0
		assert applicationResults.size() > 0
	}

	def "With Application Filter Selection of Artifacts"() {
		setup:
		def artifactManager = ArtifactManager.create("com.affichage.common.maven","dm-bom","target/maverepo")
		when:
		def results = artifactManager.getAllDependencies("9.0.6.ADMIN-UIMIG-SNAPSHOT",SearchCondition.APPLICATION)
		def nonApgResults = results.findAll{ (!it.groupId.startsWith("com.apgsga") && ! it.groupId.startsWith("com.affichage"))}
		then:
		assert results.size() > 0
		assert nonApgResults.size() == 0
	}
	
	def "Clean local Mavenrepo"() {
		setup:
		def artifactManager = ArtifactManager.create("com.affichage.common.maven","dm-bom","target/maverepo")
		when:
		def results = artifactManager.getAllDependencies("9.0.6.ADMIN-UIMIG-SNAPSHOT",SearchCondition.APPLICATION)
		def numberOfFilesBefore = artifactManager.getMavenLocalRepo().listFiles().length
		artifactManager.cleanLocalMavenRepo()
		def numberOfFilesAfter = artifactManager.getMavenLocalRepo().listFiles().length
		then:
		assert results.size() > 0
		assert numberOfFilesBefore > 0
		assert numberOfFilesAfter == 0
	}
}
