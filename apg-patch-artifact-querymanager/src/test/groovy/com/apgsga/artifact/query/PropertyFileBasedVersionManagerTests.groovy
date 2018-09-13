package com.apgsga.artifact.query
import java.lang.ClassLoader.ParallelLoaders
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.springframework.core.io.FileSystemResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.util.FileSystemUtils
import com.apgsga.artifact.query.impl.PropertyFileBasedVersionManager
import com.apgsga.microservice.patch.api.SearchCondition
import com.google.common.collect.Count
import groovy.json.JsonSlurper
import spock.lang.Specification;

class PropertyFileBasedVersionManagerTests extends Specification {

	def "Without additional MavenArtefact List"() {
		setup:
		def rl = new FileSystemResourceLoader();
		def resource = rl.getResource("target/maverepo");
		def artifactManager = new PropertyFileBasedVersionManager(resource.getURI(),"com.affichage.common.maven","dm-bom")
		when:
		def result = artifactManager.getVersionFor("com.affichage.it21.vk","zentraldispo-ui","9.0.6.ADMIN-UIMIG-SNAPSHOT")
		then:
		assert result.equals("9.0.6.ADMIN-UIMIG-SNAPSHOT")
	}
	
	def "With additional MavenArtefact List Overriding Versions"() {
		setup:
		def rl = new FileSystemResourceLoader();
		def mavenRepoResource = rl.getResource("target/maverepo");
		def patchFile = rl.getResource("classpath:Patch5797.json").getFile()
		def patch = new JsonSlurper().parseText(patchFile.text)
		def artifactManager = new PropertyFileBasedVersionManager(mavenRepoResource.getURI(),"com.affichage.common.maven","dm-bom", patch.mavenArtifacts)
		when:
		def resultZentralDispo = artifactManager.getVersionFor("com.affichage.it21.vk","zentraldispo-ui","9.0.6.ADMIN-UIMIG-SNAPSHOT")
		def resultCommondao = artifactManager.getVersionFor("com.affichage.it21.adgis","adgis-common-dao","9.0.6.ADMIN-UIMIG-SNAPSHOT")
		then:
		assert resultZentralDispo.equals("9.0.6.ADMIN-UIMIG-SNAPSHOT")
		assert resultCommondao.equals("9.9.9")
	}
	

}
