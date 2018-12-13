package com.apgsga.artifact.query
import java.lang.ClassLoader.ParallelLoaders
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.test.context.ContextConfiguration
import org.springframework.util.FileSystemUtils

import com.apgsga.artifact.query.impl.PatchFileAccessException
import com.apgsga.artifact.query.impl.PropertyFileBasedVersionManager
import com.apgsga.artifact.query.impl.RepositorySystemFactoryImpl
import com.apgsga.microservice.patch.api.SearchCondition
import com.apgsga.test.config.TestConfig
import com.google.common.collect.Count
import groovy.json.JsonSlurper
import spock.lang.Specification;

@ContextConfiguration(classes = TestConfig.class)
class PropertyFileBasedVersionManagerTests extends Specification {
	
	@Value('${mavenrepo.user.name}')
	def repoUser
	
	@Value('${mavenrepo.baseurl}')
	def repoUrl
	
	@Value('${mavenrepo.name}')
	def repoName
	
	@Value('${mavenrepo.user.encryptedPwd}')
	def mavenRepoUserEncryptedPwd

	@Value('${mavenrepo.user.decryptpwd.key:}')
	def mavenRepoUserDecryptKey;

	def RepositorySystemFactory systemFactory
	
	def setup() {
		systemFactory = RepositorySystemFactory.create(repoUrl, repoName, repoUser, mavenRepoUserEncryptedPwd, mavenRepoUserDecryptKey);
	}

	def "Without additional path to Patch File "() {
		setup:
		def rl = new FileSystemResourceLoader();
		def resource = rl.getResource("target/maverepo");
		def artifactManager = ArtifactVersionManager.create(resource.getURI(),"com.affichage.common.maven","dm-bom", systemFactory)
		when:
		def result = artifactManager.getVersionFor("com.affichage.it21.vk","zentraldispo-ui","9.1.0.ADMIN-UIMIG-SNAPSHOT")
		then:
		assert result.equals("9.1.0.ADMIN-UIMIG-SNAPSHOT")
	}
	def "With empty File path to Patch File"() {
		setup:
		def rl = new FileSystemResourceLoader();
		def resource = rl.getResource("target/maverepo");
		def artifactManager = ArtifactVersionManager.create(resource.getURI(),"com.affichage.common.maven","dm-bom", systemFactory)
		when:
		def result = artifactManager.getVersionFor("com.affichage.it21.vk","zentraldispo-ui","9.1.0.ADMIN-UIMIG-SNAPSHOT")
		then:
		assert result.equals("9.1.0.ADMIN-UIMIG-SNAPSHOT")
	}
	
	def "With additional Path to PatchFile Overriding Version Numbers"() {
		setup:
		def rl = new FileSystemResourceLoader();
		def mavenRepoResource = rl.getResource("target/maverepo");
		def artifactManager = ArtifactVersionManager.create(mavenRepoResource.getURI(),"com.affichage.common.maven","dm-bom", "src/test/resources/Patch5797.json", systemFactory)
		when:
		def resultZentralDispo = artifactManager.getVersionFor("com.affichage.it21.vk","zentraldispo-ui","9.1.0.ADMIN-UIMIG-SNAPSHOT")
		def resultCommondao = artifactManager.getVersionFor("com.affichage.it21.adgis","adgis-common-dao","9.1.0.ADMIN-UIMIG-SNAPSHOT")
		then:
		assert resultZentralDispo.equals("9.1.0.ADMIN-UIMIG-SNAPSHOT")
		assert resultCommondao.equals("9.9.9")
	}
	
	def "With additional Path to PatchFile with new Artifact Version Numbers"() {
		setup:
		def rl = new FileSystemResourceLoader();
		def mavenRepoResource = rl.getResource("target/maverepo");
		def artifactManager = ArtifactVersionManager.create(mavenRepoResource.getURI(),"com.affichage.common.maven","dm-bom", "src/test/resources/Patch5799.json", systemFactory)
		when:
		def resultNewArtifactId = artifactManager.getVersionFor("newGroupid","newArtifactID","9.1.0.ADMIN-UIMIG-SNAPSHOT")
		def resultZentraiDispoDao = artifactManager.getVersionFor("com.affichage.it21.vk","zentraldispo-dao","9.1.0.ADMIN-UIMIG-SNAPSHOT")
		then:
		assert resultNewArtifactId.equals("9.9.9.ADMIN-UIMIG-SNAPSHOT")
		assert resultZentraiDispoDao.equals("9.1.0.ADMIN-UIMIG-SNAPSHOT")
	}
	
	def "With invalid  Path to PatchFile Overriding Version Numbers"() {
		setup:
		def rl = new FileSystemResourceLoader();
		def mavenRepoResource = rl.getResource("target/maverepo");
		def artifactManager = ArtifactVersionManager.create(mavenRepoResource.getURI(),"com.affichage.common.maven","dm-bom", "xxxxxxxx/Patch5797.json", systemFactory)
		when:
		def resultZentralDispo = artifactManager.getVersionFor("com.affichage.it21.vk","zentraldispo-ui","9.1.0.ADMIN-UIMIG-SNAPSHOT")
		def resultCommondao = artifactManager.getVersionFor("com.affichage.it21.adgis","adgis-common-dao","9.1.0.ADMIN-UIMIG-SNAPSHOT")
		then:
		thrown PatchFileAccessException
	}
	

}
