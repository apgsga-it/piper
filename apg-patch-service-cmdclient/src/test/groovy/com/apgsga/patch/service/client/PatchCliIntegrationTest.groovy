package com.apgsga.patch.service.client;

import org.junit.Rule
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource;

import com.apgsga.microservice.patch.api.DbModules
import com.apgsga.microservice.patch.api.Patch
import com.apgsga.microservice.patch.api.PatchPersistence
import com.apgsga.microservice.patch.api.ServicesMetaData
import com.apgsga.microservice.patch.server.MicroPatchServer;
import com.apgsga.patch.service.client.revision.PatchRevisionCli
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import spock.lang.Ignore
import spock.lang.Specification;

@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = [MicroPatchServer.class])
@TestPropertySource(locations = ["classpath:config/server-test.properties"])
@ActiveProfiles("test,mock,mockMavenRepo,groovyactions")
public class PatchCliIntegrationTest extends Specification {

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
		System.properties['appPropertiesFile'] = 'classpath:config/app-test.properties'
		System.properties['opsPropertiesFile'] = 'classpath:config/ops-test.properties'
	}

	def "Patch Cli should print out help without errors"() {
		def result = PatchCli.create().process(["-h"])
		expect: "PatchCli returns null in case of help only (-h)"
		result != null
		result.returnCode == 0
		result.results.size() == 0
	}

	def "Patch Cli should print out help without errors in case of no options "() {
		def result = PatchCli.create().process([])
		expect: "PatchCli returns null in case no options entered"
		result != null
		result.returnCode == 0
		result.results.size() == 0
	}

	def "Patch Cli queries existance of not existing Patch and returns false"() {
		setup:
		def client = PatchCli.create()
		when:
		def result = client.process(["-e", "9999"])
		then:
		result != null
		result.returnCode == 0
		result.results['e'].exists == false
	}

	def "Patch Cli copies Patch File to server and queries before and after existence"() {
		setup:
		def client = PatchCli.create()
		when:
		def preCondResult = client.process(["-e", "5401"])
		def result = client.process(["-s", "src/test/resources/Patch5401.json"])
		def postCondResult = client.process(["-e", "5401"])

		then:
		preCondResult != null
		preCondResult.returnCode == 0
		preCondResult.results['e'].exists == false
		result != null
		result.returnCode == 0
		postCondResult != null
		postCondResult.returnCode == 0
		postCondResult.results['e'].exists == true
		def dbFile = new File("${dbLocation}/Patch5401.json")
		def sourceFile = new File("src/test/resources/Patch5401.json")
		ObjectMapper mapper = new ObjectMapper();
		mapper.readValue(sourceFile,Patch.class).equals(mapper.readValue(dbFile,Patch.class))
		cleanup:
		repo.clean()
	}
	
	def "Patch Cli saves with -sa Patch File to server and queries before and after existence"() {
		setup:
		def client = PatchCli.create()
		when:
		def preCondResult = client.process(["-e", "5401"])
		def result = client.process(["-sa", "src/test/resources/Patch5401.json"])
		def postCondResult = client.process(["-e", "5401"])

		then:
		preCondResult != null
		preCondResult.returnCode == 0
		preCondResult.results['e'].exists == false
		result != null
		result.returnCode == 0
		postCondResult != null
		postCondResult.returnCode == 0
		postCondResult.results['e'].exists == true
		def dbFile = new File("${dbLocation}/Patch5401.json")
		def sourceFile = new File("src/test/resources/Patch5401.json")
		ObjectMapper mapper = new ObjectMapper();
		mapper.readValue(sourceFile,Patch.class).equals(mapper.readValue(dbFile,Patch.class))
		cleanup:
		repo.clean()
	}
	
	def "Patch Cli redo's Patch, which has been saved before with -sa"() {
		setup:
		def client = PatchCli.create()
		when:
		def preCondResult1 = client.process(["-e", "5401"])
		def preCondResult2 = client.process(["-sa", "src/test/resources/Patch5401.json"])
		def preCondResult3 = client.process(["-e", "5401"])
		def result = client.process(["-redo", "5401"])

		then:
		preCondResult1 != null
		preCondResult1.returnCode == 0
		preCondResult1.results['e'].exists == false
		preCondResult2 != null
		preCondResult2.returnCode == 0
		preCondResult3 != null
		preCondResult3.returnCode == 0
		preCondResult3.results['e'].exists == true
		result != null
		result.returnCode == 0
		def dbFile = new File("${dbLocation}/Patch5401.json")
		def sourceFile = new File("src/test/resources/Patch5401.json")
		ObjectMapper mapper = new ObjectMapper();
		mapper.readValue(sourceFile,Patch.class).equals(mapper.readValue(dbFile,Patch.class))
		cleanup:
		repo.clean()
	}

	def "Patch Cli return found = false on findById of non existing Patch"() {
		setup:
		def client = PatchCli.create()
		when:
		def preCondResult = client.process(["-e", "5401"])
		def result = client.process(["-f", "5401,build"])

		then:
		preCondResult != null
		preCondResult.returnCode == 0
		preCondResult.results['e'].exists == false
		result != null
		result.returnCode == 0
		result.results['f'].exists == false
	}

	def "Patch Cli return found on findById on Patch, which been copied before"() {
		setup:
		def client = PatchCli.create()
		when:
		def preCondResult = client.process(["-s", "src/test/resources/Patch5401.json"])
		def result = client.process(["-f", "5401,build"])

		then:
		preCondResult != null
		preCondResult.returnCode == 0
		result != null
		result.returnCode == 0
		result.results.size() == 1
		result.results['f'].exists == true
		def dbFile = new File("${dbLocation}/Patch5401.json")
		def copiedFile = new File("build/Patch5401.json")
		ObjectMapper mapper = new ObjectMapper();
		mapper.readValue(dbFile,Patch.class).equals(mapper.readValue(copiedFile,Patch.class))
		cleanup:
		repo.clean()
	}

	def "Patch Cli removes Patch, which been copied before"() {
		setup:
		def client = PatchCli.create()
		when:
		def preCondResult = client.process(["-s", "src/test/resources/Patch5401.json"])
		def result = client.process(["-r", "5401"])
		def postCondResult = client.process(["-e", "5401"])
		then:
		preCondResult != null
		preCondResult.returnCode == 0
		result != null
		result.returnCode == 0
		postCondResult != null
		postCondResult.returnCode == 0
		postCondResult.results['e'].exists == false
		cleanup:
		repo.clean()
	}

	def "Patch Cli upload DbModules to server"() {
		setup:
		def client = PatchCli.create()
		when:
		def result = client.process(["-ud", "src/test/resources/DbModules.json"])
		then:
		result != null
		result.returnCode == 0
		def dbFile = new File("${dbLocation}/DbModules.json")
		def sourceFile = new File("src/test/resources/DbModules.json")
		ObjectMapper mapper = new ObjectMapper();
		mapper.readValue(dbFile,DbModules.class).equals(mapper.readValue(sourceFile,DbModules.class))
		cleanup:
		repo.clean()
	}

	def "Patch Cli download DbModules from server"() {
		setup:
		def client = PatchCli.create()
		when:
		def preConResult = client.process(["-ud", "src/test/resources/DbModules.json"])
		def result = client.process(["-dd", "build"])
		then:
		preConResult != null
		preConResult.returnCode == 0
		result != null
		result.returnCode == 0
		def dbFile = new File("${dbLocation}/DbModules.json")
		def copiedFile = new File("build/DbModules.json")
		ObjectMapper mapper = new ObjectMapper();
		mapper.readValue(dbFile,DbModules.class).equals(mapper.readValue(copiedFile,DbModules.class))
		cleanup:
		repo.clean()
	}

	def "Patch Cli download DbModules from server, where it does'nt exist"() {
		setup:
		def client = PatchCli.create()
		when:
		def result = client.process(["-dd", "build"])
		then:
		result != null
		result.returnCode == 0
		result.results['dd'].exists == false
	}

	def "Patch Cli upload ServiceMetaData to server"() {
		setup:
		def client = PatchCli.create()
		when:
		def result = client.process(["-um", "src/test/resources/ServicesMetaData.json"])
		then:
		result != null
		result.returnCode == 0
		def dbFile = new File("${dbLocation}/ServicesMetaData.json")
		def sourceFile = new File("src/test/resources/ServicesMetaData.json")
		ObjectMapper mapper = new ObjectMapper();
		mapper.readValue(dbFile,  ServicesMetaData.class).equals(mapper.readValue(sourceFile, ServicesMetaData.class))
		cleanup:
		repo.clean()
	}

	def "Patch Cli download ServiceMetaData from server"() {
		setup:
		def client = PatchCli.create()
		when:
		def preConResult = client.process(["-um", "src/test/resources/ServicesMetaData.json"])
		def result = client.process(["-dm", "build"])
		then:
		preConResult != null
		preConResult.returnCode == 0
		result != null
		result.returnCode == 0
		def dbFile = new File("${dbLocation}/ServicesMetaData.json")
		def copiedFile = new File("build/ServicesMetaData.json")
		ObjectMapper mapper = new ObjectMapper();
		mapper.readValue(dbFile,  ServicesMetaData.class).equals(mapper.readValue(copiedFile, ServicesMetaData.class))
		cleanup:
		repo.clean()
	}


	def "Patch Cli download ServiceMetaData from server, where it does'nt exist"() {
		setup:
		def client = PatchCli.create()
		when:
		def result = client.process(["-dm", "build"])
		then:
		result != null
		result.returnCode == 0
		result.results['dm'].exists == false
	}

	def "Patch Cli invalid State Change Action"() {
		setup:
		def client = PatchCli.create()
		when:
		def result = client.process(["-sta", "9999,XXXXXX,aps"])
		then:
		result != null
		result.returnCode == 0
	}


	def "Patch Cli valid State Change Action for config aps"() {
		setup:
		def client = PatchCli.create()
		when:
		def preCondResult = client.process(["-s", "src/test/resources/Patch5401.json"])
		def result = client.process(["-sta", '5401,EntwicklungInstallationsbereit,aps'])
		then:
		preCondResult != null
		preCondResult.returnCode == 0
		result != null
		result.returnCode == 0
		cleanup:
		repo.clean()
	}

	def "Patch Cli valid State Change Action for config nil"() {
		setup:
		def client = PatchCli.create()
		when:
		def preCondResult = client.process(["-s", "src/test/resources/Patch5401.json"])
		def result = client.process(["-sta", '5401,EntwicklungInstallationsbereit,nil'])
		then:
		preCondResult != null
		preCondResult.returnCode == 0
		result != null
		result.returnCode == 0
		cleanup:
		repo.clean()
	}

	def "Patch Cli Missing configuration for State Change Action"() {
		setup:
		def client = PatchCli.create()
		when:
		def result = client.process(["-sta", "9999,EntwicklungInstallationsbereit"])
		then:
		result != null
		result.returnCode == 0
	}
}
