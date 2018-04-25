package com.apgsga.patch.service.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.FileSystemResourceLoader
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.context.WebApplicationContext;

import com.apgsga.microservice.patch.api.DbModules
import com.apgsga.microservice.patch.api.Patch
import com.apgsga.microservice.patch.api.PatchOpService
import com.apgsga.microservice.patch.api.PatchPersistence
import com.apgsga.microservice.patch.api.ServiceMetaData
import com.apgsga.microservice.patch.api.ServicesMetaData
import com.apgsga.microservice.patch.api.TargetSystemEnviroment
import com.apgsga.microservice.patch.api.TargetSystemEnvironments
import com.apgsga.microservice.patch.api.impl.TargetSystemEnviromentBean
import com.apgsga.microservice.patch.server.MicroPatchServer;
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap.Empty
import com.google.common.collect.Lists

import spock.lang.Specification;

@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = [MicroPatchServer.class ])
@TestPropertySource(locations = "application-test.properties")
@ActiveProfiles("test,mock")
public class IntegrationTest extends Specification {


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

	def "Patch Cli should print out help without errors"() {
		def opts = PatchCli.create().process(["-h"])
		expect: "PatchCli returns null in case of help only (-h)"
		opts == null
	}

	def "Patch Cli should print out help without errors in case of no options "() {
		def opts = PatchCli.create().process([])
		expect: "PatchCli returns null in case no options entered"
		opts == null
	}

	def "Patch Cli queries existance of not existing Patch and returns false"() {
		setup:
			def client = PatchCli.create()
		when:
			def result = client.process(["-u", baseUrl, "-e", "9999"])
		then: 
			result != null
			result.returnCode == 0
			result.results['e'].exists == false
	}

	def "Patch Cli copies Patch File to server and queries before and after existence"() {
		setup:
			def client = PatchCli.create()
		when:
			def preCondResult = client.process(["-u", baseUrl, "-e", "5401"])
			def result = client.process(["-u", baseUrl, "-s", "src/test/resources/Patch5401.json"])
			def postCondResult = client.process(["-u", baseUrl, "-e", "5401"])
			
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
	
	def "Patch Cli return found = false on findById of non existing Patch"() {
		setup:
			def client = PatchCli.create()
		when:
			def preCondResult = client.process(["-u", baseUrl, "-e", "5401"])
			def result = client.process(["-u", baseUrl, "-f", "5401,build"])
			
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
			def preCondResult = client.process(["-u", baseUrl, "-s", "src/test/resources/Patch5401.json"])
			def result = client.process(["-u", baseUrl, "-f", "5401,build"])
			
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
			def preCondResult = client.process(["-u", baseUrl, "-s", "src/test/resources/Patch5401.json"])
			def result = client.process(["-u", baseUrl, "-r", "5401"])
			def postCondResult = client.process(["-u", baseUrl, "-e", "5401"])
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
			def result = client.process(["-u", baseUrl, "-ud", "src/test/resources/DbModules.json"])
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
			def preConResult = client.process(["-u", baseUrl, "-ud", "src/test/resources/DbModules.json"])
			def result = client.process(["-u", baseUrl, "-dd", "build"])
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
			def result = client.process(["-u", baseUrl, "-dd", "build"])
		then:
			result != null
			result.returnCode == 0
			result.results['dd'].exists == false
			
	}
	
	def "Patch Cli upload ServiceMetaData to server"() {
		setup:
			def client = PatchCli.create()
		when:
			def result = client.process(["-u", baseUrl, "-um", "src/test/resources/ServicesMetaData.json"])
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
			def preConResult = client.process(["-u", baseUrl, "-um", "src/test/resources/ServicesMetaData.json"])
			def result = client.process(["-u", baseUrl, "-dm", "build"])
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
	
	def "Patch Cli upload TargetSystemEnvironments to server"() {
		setup:
			def client = PatchCli.create()
		when:
			def result = client.process(["-u", baseUrl, "-ut", "src/test/resources/TargetSystemEnvironments.json"])
		then:
			result != null
			result.returnCode == 0
			def dbFile = new File("${dbLocation}/InstallationTargets.json")
			def sourceFile = new File("src/test/resources/TargetSystemEnvironments.json")
			ObjectMapper mapper = new ObjectMapper();
			TargetSystemEnviromentBean[] targetsDb = mapper.readValue(dbFile, TargetSystemEnviromentBean[].class);		
			def targetsDbList = Lists.newArrayList(targetsDb)
			TargetSystemEnvironments source = mapper.readValue(sourceFile, TargetSystemEnvironments.class)
			def sourceList = source.getTargetSystemEnviroments();
			sourceList.equals(targetsDbList)
		 cleanup:
		 	repo.clean()
	}
	
	def "Patch Cli download TargetSystemEnvironments from server"() {
		setup:
			def client = PatchCli.create()
		when:
			def preConResult = client.process(["-u", baseUrl, "-ut", "src/test/resources/TargetSystemEnvironments.json"])
			def result = client.process(["-u", baseUrl, "-dt", "build"])
		then:
			preConResult != null
			preConResult.returnCode == 0
			result != null
			result.returnCode == 0
			def dbFile = new File("${dbLocation}/InstallationTargets.json")
			ObjectMapper mapper = new ObjectMapper();
			TargetSystemEnviromentBean[] targetsDb = mapper.readValue(dbFile, TargetSystemEnviromentBean[].class);
			def targetsDbList = Lists.newArrayList(targetsDb)
			def copiedFile = new File("build/TargetSystemEnvironments.json")
			TargetSystemEnvironments source = mapper.readValue(copiedFile, TargetSystemEnvironments.class)
			def sourceList = source.getTargetSystemEnviroments();
			sourceList.equals(targetsDbList)
		 cleanup:
			repo.clean()
	}
	
	def "Patch Cli download ServiceMetaData from server, where it does'nt exist"() {
		setup:
			def client = PatchCli.create()
		when:
			def result = client.process(["-u", baseUrl, "-dm", "build"])
		then:
			result != null
			result.returnCode == 0
			result.results['dm'].exists == false
	}
	
	def "Patch Cli invalid State Change Action"() {
		setup:
			def client = PatchCli.create()
		when:
			def result = client.process(["-u", baseUrl, "-sta", "9999,XXXXXX,aps"])
		then:
			result == null
	}
	
	def "Patch Cli Missing configuration for State Change Action"() {
		setup:
			def client = PatchCli.create()
		when:
			def result = client.process(["-u", baseUrl, "-sta", "9999,EntwicklungInstallationsbereit"])
		then:
			result == null
	}
	
	def "Patch Cli Invalid configuration for State Change Action"() {
		setup:
			def client = PatchCli.create()
		when:
			def result = client.process(["-u", baseUrl, "-sta", "9999,EntwicklungInstallationsbereit,xxxxx"])
		then:
			result == null
	}
	
	def "Patch Cli valid State Change Action for config aps"() {
		setup:
			def client = PatchCli.create()
		when:
			def preCondResult = client.process(["-u", baseUrl, "-s", "src/test/resources/Patch5401.json"])
			def result = client.process(["-u", baseUrl, "-sta", "5401,EntwicklungInstallationsbereit,aps"])
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
			def preCondResult = client.process(["-u", baseUrl, "-s", "src/test/resources/Patch5401.json"])
			def result = client.process(["-u", baseUrl, "-sta", "5401,EntwicklungInstallationsbereit,nil"])
		then:
			preCondResult != null
			preCondResult.returnCode == 0
			result != null
			result.returnCode == 0
		cleanup:
			repo.clean()
	}
	
	def "Patch Cli valid State Change Action for config db with config file"() {
		setup:
			def client = PatchCli.create()
		when:
			def preCondResult = client.process(["-u", baseUrl, "-s", "src/test/resources/Patch5401.json"])
			def result = client.process(["-u", baseUrl, "-sta", "5401,EntwicklungInstallationsbereit,mockdb", "-db", "src/main/resources/config/defaults.groovy"])
		then:
			preCondResult != null
			preCondResult.returnCode == 0
			result != null
			result.returnCode == 0
		cleanup:
			repo.clean()
	}
	
	def "Patch Cli validate Artifact names from version"() {
		setup:
			def client = PatchCli.create()
		when:
			def result = client.process(["-u", baseUrl, "-vv", "9.0.6.ADMIN-UIMIG-SNAPSHOT"])
		then:
			result != null
			result.returnCode == 0
	}
	
	def "Patch Cli validate Artifact names contained within a Patch"() {
		setup:
			def client = PatchCli.create()
		when:
			def preCondResult = client.process(["-u", baseUrl, "-s", "src/test/resources/Patch5401.json"])
			def result = client.process((["-u", baseUrl, "-vp", "5401"]))
		then:
			preCondResult != null
			preCondResult.returnCode == 0
			result != null
			result.returnCode == 0
	}	
}
