package com.apgsga.patch.service.client

import com.apgsga.microservice.patch.api.PatchPersistence
import com.apgsga.microservice.patch.server.MicroPatchServer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification

@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = [MicroPatchServer.class ])
@TestPropertySource(locations = ["classpath:config/server-test.properties"])
@ActiveProfiles("test,mock,mockMavenRepo,groovyactions")
class PatchCliIntegrationExceptionHandlingTests extends Specification {


	def setup() {
		def buildFolder = new File("build")
		if (!buildFolder.exists()) {
			def created = buildFolder.mkdir()
			println ("Buildfolder has been created ${created}")
		}
		System.properties['spring_profiles_active'] = 'default'
		System.properties['appPropertiesFile'] = 'classpath:config/app-test.properties'
		System.properties['opsPropertiesFile'] = 'classpath:config/ops-test.properties'
	}

	def "Patch Cli should print Server Exception and return returnCode > 0 for invalid findById"() {
		setup:
		def client = PatchCli.create()
		client.validate = false
		when:
		def result = client.process(["-f", " ,build"])
		then:
		result != null
		result.returnCode >  0
		result.results.containsKey('error') == true
		result.results['error'].errorKey == "FilebasedPatchPersistence.findById.patchnumber.notnullorempty.assert"
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
	
	def "Patch Cli should print Server Exception and return returnCode > 0  with Patchnumber empty for State Change Action"() {
		setup:
		def client = PatchCli.create()
		client.validate = false
		when:
		def result = client.process(["-sta", "   ,EntwicklungInstallationsbereit,aps"])
		then:
		result.returnCode >  0
		result.results.containsKey('error') == true
		result.results['error'].errorKey == "GroovyScriptActionExecutor.execute.patchnumber.notnullorempty.assert"
	}
	

	def "Patch Cli should print Server Exception and return returnCode > 0  with Patch for for State Change Action does not exist"() {
		setup:
		def client = PatchCli.create()
		when:
		def result = client.process(["-sta", "9999,EntwicklungInstallationsbereit,aps"])
		then:
		result.returnCode >  0
		result.results.containsKey('error') == true
		result.results['error'].errorKey == "GroovyScriptActionExecutor.execute.patch.exists.assert"
	}
}
