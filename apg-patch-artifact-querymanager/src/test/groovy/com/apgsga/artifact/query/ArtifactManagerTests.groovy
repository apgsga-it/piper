package com.apgsga.artifact.query
import java.lang.ClassLoader.ParallelLoaders
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.springframework.core.io.FileSystemResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.util.FileSystemUtils

import com.apgsga.microservice.patch.api.MavenArtifact
import com.apgsga.microservice.patch.api.SearchCondition
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Count
import groovy.json.JsonBuilder
import spock.lang.Specification;

class ArtifactManagerTests extends Specification {

	def "Default Filter Selection of Artifacts"() {
		setup:
		def artifactManager = ArtifactManager.create("com.affichage.common.maven","dm-bom","target/maverepo")
		when:
		def results = artifactManager.getAllDependencies("9.1.0.ADMIN-UIMIG-SNAPSHOT")
		def nonApgResults = results.findAll{ (!it.groupId.startsWith("com.apgsga") && ! it.groupId.startsWith("com.affichage"))}
		then:
		assert results.size() > 0
		assert nonApgResults.size() == 0
	}



	def "With All Filter Selection of Artifacts"() {
		setup:
		def artifactManager = ArtifactManager.create("com.affichage.common.maven","dm-bom","target/maverepo")
		when:
		def results = artifactManager.getAllDependencies("9.1.0.ADMIN-UIMIG-SNAPSHOT",SearchCondition.ALL)
		println results.size()
		def nonApplicationResults = results.findAll{ (!it.groupId.startsWith("com.apgsga") && ! it.groupId.startsWith("com.affichage"))}
		def applicationResults = results.findAll{ (it.groupId.startsWith("com.apgsga") ||  it.groupId.startsWith("com.affichage"))}
		then:
		assert results.size() > 0
		assert nonApplicationResults.size() > 0
		assert applicationResults.size() > 0
	}

	def "With Application Filter Selection IT21UI of Artifacts"() {
		setup:
		def artifactManager = ArtifactManager.create("com.affichage.common.maven","dm-bom","target/maverepo")
		when:
		def results = artifactManager.getAllDependencies("9.1.0.ADMIN-UIMIG-SNAPSHOT",SearchCondition.IT21UI)
		ObjectMapper mapper = new ObjectMapper();
		def expectedTemplate = mapper.readValue(new File("src/test/resources/templateIt21Ui.json"),MavenArtifact[].class)
		then:
		assert results.size() > 0
		assert results.equals(expectedTemplate)
	}
	
	def "With Application Filter Selection Persistence of Artifacts"() {
		setup:
		def artifactManager = ArtifactManager.create("com.affichage.common.maven","dm-bom","target/maverepo")
		when:
		def results = artifactManager.getAllDependencies("9.1.0.ADMIN-UIMIG-SNAPSHOT",SearchCondition.PERSISTENT).toSorted()
		ObjectMapper mapper = new ObjectMapper();
		def expectedTemplate = Arrays.asList(mapper.readValue(new File("src/test/resources/templatePersistence.json"),MavenArtifact[].class)).toSorted()
		then:
		assert results.size() == 5
		assert results.equals(expectedTemplate)
	}
	
	def "With Application Filter Selection Forms2Java of Artifacts"() {
		setup:
		def artifactManager = ArtifactManager.create("com.affichage.common.maven","dm-bom","target/maverepo")
		when:
		def results = artifactManager.getAllDependencies("9.1.0.ADMIN-UIMIG-SNAPSHOT",SearchCondition.FORMS2JAVA).toSorted()
		ObjectMapper mapper = new ObjectMapper();
		def expectedTemplate = Arrays.asList(mapper.readValue(new File("src/test/resources/templateForms2Java.json"),MavenArtifact[].class)).toSorted()
		then:
		assert results.size() == 15
		assert results.equals(expectedTemplate)
	}
	
	def "With Application Filter Selection of Artifacts"() {
		setup:
		def artifactManager = ArtifactManager.create("com.affichage.common.maven","dm-bom","target/maverepo")
		when:
		def results = artifactManager.getAllDependencies("9.1.0.ADMIN-UIMIG-SNAPSHOT",SearchCondition.APPLICATION)
		def nonApgResults = results.findAll{ (!it.groupId.startsWith("com.apgsga") && ! it.groupId.startsWith("com.affichage"))}
		then:
		assert results.size() > 0
		assert nonApgResults.size() == 0
	}
	
	def "Clean local Mavenrepo"() {
		setup:
		def artifactManager = ArtifactManager.create("com.affichage.common.maven","dm-bom","target/maverepo")
		when:
		def results = artifactManager.getAllDependencies("9.1.0.ADMIN-UIMIG-SNAPSHOT",SearchCondition.APPLICATION)
		def numberOfFilesBefore = artifactManager.getMavenLocalRepo().listFiles().length
		artifactManager.cleanLocalMavenRepo()
		def localRepo = artifactManager.getMavenLocalRepo(); 
		def comAffichage = new File(localRepo, "com/affichage")
		def comAffichageNumberOfFilesAfter = comAffichage.listFiles().length
		def comApgsga = new File(localRepo, "com/apgsga")
		def comApgsgaNumberOfFilesAfter = comApgsga.listFiles().length
		then:
		assert results.size() > 0
		assert numberOfFilesBefore > 0
		assert comAffichageNumberOfFilesAfter == 0
		assert comApgsgaNumberOfFilesAfter == 0
	}
}
