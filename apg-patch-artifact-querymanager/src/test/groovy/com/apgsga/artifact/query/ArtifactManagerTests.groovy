package com.apgsga.artifact.query

import com.apgsga.microservice.patch.api.MavenArtifact
import com.apgsga.microservice.patch.api.SearchCondition
import com.apgsga.test.config.TestConfig
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResourceLoader
import org.springframework.core.io.ResourceLoader
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(classes = TestConfig.class)
class ArtifactManagerTests extends Specification {
	
	@Value('${mavenrepo.user.name}')
	def repoUser
	
	@Value('${mavenrepo.baseurl}')
	def repoUrl
	
	@Value('${mavenrepo.name}')
	def repoName
	
	@Value('${mavenrepo.user.encryptedPwd}')
	def mavenRepoUserEncryptedPwd
	
	@Value('${mavenrepo.user.decryptpwd.key:}')
	def mavenRepoUserDecryptKey

	RepositorySystemFactory systemFactory
	
	def setup() {
		systemFactory = RepositorySystemFactory.create(repoUrl, repoName, repoUser, mavenRepoUserEncryptedPwd, mavenRepoUserDecryptKey)
	}
	
	def "Default Filter Selection of Artifacts"() {
		setup:
		def bomCoordinates = MavenArtifact.builder()
				.groupId("com.affichage.common.maven")
				.artifactId("dm-bom")
				.version("9.1.0.ADMIN-UIMIG-SNAPSHOT").build()
		def artifactManager = ArtifactManager.create("target/maverepo", systemFactory)
		when:
		def results = artifactManager.listDependenciesInBom(bomCoordinates)
		def nonApgResults = results.findAll{ (!it.groupId.startsWith("com.apgsga") && ! it.groupId.startsWith("com.affichage"))}
		then:
		assert results.size() > 0
		assert nonApgResults.size() == 0
	}



	def "With All Filter Selection of Artifacts"() {
		setup:
		def bomCoordinates = MavenArtifact.builder()
				.groupId("com.affichage.common.maven")
				.artifactId("dm-bom")
				.version("9.1.0.ADMIN-UIMIG-SNAPSHOT").build()
		def artifactManager = ArtifactManager.create("target/maverepo", systemFactory)
		when:
		def results = artifactManager.listDependenciesInBom(bomCoordinates,SearchCondition.ALL)
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
		def bomCoordinates = MavenArtifact.builder()
				.groupId("com.affichage.common.maven")
				.artifactId("dm-bom")
				.version("9.1.0.ADMIN-UIMIG-SNAPSHOT").build()
		def artifactManager = ArtifactManager.create("target/maverepo", systemFactory)
		when:
		def results = artifactManager.listDependenciesInBom(bomCoordinates,SearchCondition.IT21UI)
		ObjectMapper mapper = new ObjectMapper()
		def expectedTemplate = mapper.readValue(new File("src/test/resources/templateIt21Ui.json"),MavenArtifact[].class)
		then:
		assert results.size() > 0
		assert results.toArray() == expectedTemplate
	}
	
	def "With Application Filter Selection Persistence of Artifacts"() {
		setup:
		def bomCoordinates = MavenArtifact.builder()
				.groupId("com.affichage.common.maven")
				.artifactId("dm-bom")
				.version("9.1.0.ADMIN-UIMIG-SNAPSHOT").build()
		def artifactManager = ArtifactManager.create("target/maverepo", systemFactory)
		when:
		def results = artifactManager.listDependenciesInBom(bomCoordinates,SearchCondition.PERSISTENT).toSorted()
		ObjectMapper mapper = new ObjectMapper()
		def expectedTemplate = Arrays.asList(mapper.readValue(new File("src/test/resources/templatePersistence.json"),MavenArtifact[].class)).toSorted()
		then:
		assert results.size() == 5
		assert results == expectedTemplate
	}
	
	def "With Application Filter Selection Forms2Java of Artifacts"() {
		setup:
		def bomCoordinates = MavenArtifact.builder()
				.groupId("com.affichage.common.maven")
				.artifactId("dm-bom")
				.version("9.1.0.ADMIN-UIMIG-SNAPSHOT").build()
		def artifactManager = ArtifactManager.create("target/maverepo", systemFactory)
		when:
		def results = artifactManager.listDependenciesInBom(bomCoordinates,SearchCondition.FORMS2JAVA).toSorted()
		ObjectMapper mapper = new ObjectMapper()
		def expectedTemplate = Arrays.asList(mapper.readValue(new File("src/test/resources/templateForms2Java.json"),MavenArtifact[].class)).toSorted()
		then:
		assert results.size() == 15
		assert results == expectedTemplate
	}
	
	def "With Application Filter Selection of Artifacts"() {
		setup:
		def bomCoordinates = MavenArtifact.builder()
				.groupId("com.affichage.common.maven")
				.artifactId("dm-bom")
				.version("9.1.0.ADMIN-UIMIG-SNAPSHOT").build()
		def artifactManager = ArtifactManager.create("target/maverepo", systemFactory)
		when:
		def results = artifactManager.listDependenciesInBom(bomCoordinates,SearchCondition.APPLICATION)
		def nonApgResults = results.findAll{ (!it.groupId.startsWith("com.apgsga") && ! it.groupId.startsWith("com.affichage"))}
		then:
		assert results.size() > 0
		assert nonApgResults.size() == 0
	}
	
	def "Clean local Mavenrepo"() {
		setup:
		def bomCoordinates = MavenArtifact.builder()
				.groupId("com.affichage.common.maven")
				.artifactId("dm-bom")
				.version("9.1.0.ADMIN-UIMIG-SNAPSHOT").build()
		def artifactManager = ArtifactManager.create("target/maverepo", systemFactory)
		when:
		def results = artifactManager.listDependenciesInBom(bomCoordinates,SearchCondition.APPLICATION)
		final ResourceLoader rl = new FileSystemResourceLoader();
		def localRepo = rl.getResource("target/maverepo").getFile();
		def numberOfFilesBefore = localRepo.listFiles().length
		artifactManager.cleanLocalMavenRepo()
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
