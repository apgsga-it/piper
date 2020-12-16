package com.apgsga.patch.service.client

import com.apgsga.microservice.patch.api.PatchPersistence
import com.apgsga.microservice.patch.server.MicroPatchServer
import org.junit.Assert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.core.io.FileSystemResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.util.FileCopyUtils
import spock.lang.Specification

@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = [MicroPatchServer.class ])
@TestPropertySource(locations = ["classpath:config/server-test.properties"])
@ActiveProfiles("test,mock,mockMavenRepo,groovyactions")
class PatchCliIntegrationExceptionHandlingTests extends Specification {

	@Value('${json.meta.info.db.location}')
	private String metaInfoDbLocation

	def setup() {
		def buildFolder = new File("build")
		if (!buildFolder.exists()) {
			def created = buildFolder.mkdir()
			println ("Buildfolder has been created ${created}")
		}
		System.properties['spring_profiles_active'] = 'default'
		System.properties['piper.host.default.url'] = 'localhost:9020'
		try {
			final ResourceLoader rl = new FileSystemResourceLoader()
			Resource testResources = rl.getResource("src/test/resources")
			File metaInfoPersistFolder = new File(metaInfoDbLocation)
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/ServicesMetaData.json"), new File(metaInfoPersistFolder, "ServicesMetaData.json"))
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/OnDemandTargets.json"), new File(metaInfoPersistFolder, "OnDemandTargets.json"))
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/StageMappings.json"), new File(metaInfoPersistFolder, "StageMappings.json"))
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/TargetInstances.json"), new File(metaInfoPersistFolder, "TargetInstances.json"))
		} catch (IOException e) {
			Assert.fail("Unable to copy JSON test files into testDb folder",e)
		}
	}

	def "Patch Cli should be ok with returnCode == 0 for nonexisting findById"() {
		setup:
		def client = PatchCli.create()
		when:
		def result = client.process(["-f", "99999999,build"])
		then:
		result != null
		result.returnCode ==  0
	}
	
}
