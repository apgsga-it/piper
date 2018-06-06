package com.apgsga.patch.service.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource;

import com.apgsga.forms2java.persistence.mybatis.ValidateMyBatisDataAccess
import com.apgsga.microservice.patch.api.PatchPersistence
import com.apgsga.microservice.patch.server.MicroPatchServer;

import spock.lang.Specification;

@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = [MicroPatchServer.class ])
@TestPropertySource(locations = "application-test.properties")
@ActiveProfiles("test,mock,groovyactions")
public class IntegrationExceptionHandlingTests extends Specification {

	private static def DEFAULT_CONFIG_OPT = ["-c", "src/test/resources/config"]

	@Value('${baseUrl}')
	private String baseUrl;

	@Value('${json.db.location}')
	private String dbLocation;

	@Autowired
	@Qualifier("patchPersistence")
	private PatchPersistence repo;

	def setup() {
		def buildFolder = new File("build")
		if (!buildFolder.exists()) {
			def created = buildFolder.mkdir()
			println ("Buildfolder has been created ${created}")
		}
	}

	def "Patch Cli should print Server Exception and return returnCode > 0 for invalid findById"() {
		setup:
		def client = PatchCli.create()
		client.validate = false
		when:
		def result = client.process(["-u", baseUrl, "-f", " ,build"]+ DEFAULT_CONFIG_OPT)
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
		def result = client.process(["-u", baseUrl, "-f", "99999999,build"]+ DEFAULT_CONFIG_OPT)
		then:
		result != null
		result.returnCode ==  0
	}
	
	
	def "Patch Cli should print Server Exception and return returnCode > 0 for remove of notexisting Patch"() {
		setup:
		def client = PatchCli.create()
		client.validate = false
		when:
		def result = client.process(["-u", baseUrl, "-r", "XXXXX"]+ DEFAULT_CONFIG_OPT)
		then:
		result != null
		result.returnCode >  0
		result.results.containsKey('error') == true
		result.results['error'].contains("Patch XXXXX to remove not found")
	}
	
	
	def "Patch Cli should print Server Exception and return returnCode > 0 for Saven of Patch with empty Patch Number"() {
		setup:
		def client = PatchCli.create()
		client.validate = false
		when:
		def result = client.process(["-u", baseUrl, "-s", "src/test/resources/Patch5403ErrorTest.json"]+ DEFAULT_CONFIG_OPT)
		then:
		result != null
		result.returnCode >  0
		result.results.containsKey('error') == true
		result.results['error'].errorKey == "FilebasedPatchPersistence.save.patchnumber.notnullorempty.assert"
	}
	
	def "Patch Cli should print Server Exception and return returnCode > 0  with Patchnumber empty for State Change Action"() {
		setup:
		def client = PatchCli.create()
		client.validate = false
		when:
		def result = client.process(["-u", baseUrl, "-sta", "   ,EntwicklungInstallationsbereit,aps"]+ DEFAULT_CONFIG_OPT)
		then:
		result.returnCode >  0
		result.results.containsKey('error') == true
		result.results['error'].errorKey == "GroovyScriptActionExecutor.execute.patchnumber.notnullorempty.assert"
	}
	

	def "Patch Cli should print Server Exception and return returnCode > 0  with Patch for for State Change Action does not exist"() {
		setup:
		def client = PatchCli.create()
		when:
		def result = client.process(["-u", baseUrl, "-sta", "9999,EntwicklungInstallationsbereit,aps"]+ DEFAULT_CONFIG_OPT)
		then:
		result.returnCode >  0
		result.results.containsKey('error') == true
		result.results['error'].errorKey == "GroovyScriptActionExecutor.execute.patch.exists.assert"
	}
}
