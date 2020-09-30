package com.apgsga.patch.service.client

import com.apgsga.microservice.patch.api.Patch
import com.apgsga.microservice.patch.api.PatchLog
import com.apgsga.microservice.patch.api.PatchPersistence
import com.apgsga.microservice.patch.server.MicroPatchServer
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.sql.Sql
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
import spock.lang.Ignore
import spock.lang.Requires
import spock.lang.Specification

@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = [MicroPatchServer.class])
@TestPropertySource(locations = ["classpath:config/server-test.properties"])
@ActiveProfiles("test,mock,mockMavenRepo,groovyactions")
class PatchCliIntegrationTest extends Specification {

	@Value('${json.db.location}')
	private String dbLocation

	@Value('${json.meta.info.db.location}')
	private String metaInfoDbLocation

	@Autowired
	@Qualifier("patchPersistence")
	private PatchPersistence repo

	def setup() {
		def buildFolder = new File("build")
		if (!buildFolder.exists()) {
			def created = buildFolder.mkdir()
			println ("Buildfolder has been created ${created}")
		}
		System.properties['spring_profiles_active'] = 'default'
		System.properties['piper.host.default.url'] = 'localhost:9020'
		println System.getProperties()

		try {
			final ResourceLoader rl = new FileSystemResourceLoader();
			Resource testResources = rl.getResource("src/test/resources");
			File persistSt = new File(metaInfoDbLocation);
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/ServicesMetaData.json"), new File(persistSt, "ServicesMetaData.json"));
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/OnDemandTargets.json"), new File(persistSt, "OnDemandTargets.json"));
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/StageMappings.json"), new File(persistSt, "StageMappings.json"));
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/TargetInstances.json"), new File(persistSt, "TargetInstances.json"));
		} catch (IOException e) {
			Assert.fail("Unable to copy JSON test files into testDb folder");
		}
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

	def "Patch Cli saves with -sa Patch File to server and queries before and after existence"() {
		setup:
			def client = PatchCli.create()
		when:
			def result = client.process(["-sa", "src/test/resources/Patch5401.json"])
		then:
			result != null
			result.returnCode == 0
			def dbFile = new File("${dbLocation}/Patch5401.json")
			def sourceFile = new File("src/test/resources/Patch5401.json")
			ObjectMapper mapper = new ObjectMapper()
			mapper.readValue(sourceFile,Patch.class).equals(mapper.readValue(dbFile,Patch.class))
		cleanup:
			repo.clean()
	}

	def "Patch Cli saves a modification to an existing Patch"() {
		setup:
			def client = PatchCli.create()
			def sourceFile = new File("src/test/resources/Patch5401.json")
			ObjectMapper mapper = new ObjectMapper()
		when:
			def result = client.process(["-sa", "src/test/resources/Patch5401.json"])
		then:
			result != null
			result.returnCode == 0
			def dbFile = new File("${dbLocation}/Patch5401.json")
			mapper.readValue(sourceFile,Patch.class).equals(mapper.readValue(dbFile,Patch.class))
		when:
			def dbFileForChange = new File("${dbLocation}/Patch5401.json")
			Patch patchFromDb = mapper.readValue(dbFileForChange,Patch.class)
			patchFromDb.setDeveloperBranch("thisIsDeveloperBranch")
			mapper.writeValue(new File(System.getProperty("java.io.tmpdir"),"Patch5401.json"),patchFromDb)
			def result2 = client.process(["-sa", "${System.getProperty("java.io.tmpdir")}/Patch5401.json"])
		then:
			result != null
			result.returnCode == 0
			def dbFile2 = new File("${dbLocation}/Patch5401.json")
			Patch patchFromDb2 = mapper.readValue(dbFile2,Patch.class)
			patchFromDb2.developerBranch.equals("thisIsDeveloperBranch")
		cleanup:
			repo.clean()
	}

	def "Patch Cli invalid State Change Action"() {
		setup:
			def client = PatchCli.create()
		when:
			def result = client.process(["-sta", "9999,XXXXXX,aps"])
		then:
			result != null
			result.returnCode == 1
	}

	def "Patch Cli valid State Change Action for config aps"() {
		setup:
			def client = PatchCli.create()
		when:
			def preCondResult = client.process(["-sa", "src/test/resources/Patch5401.json"])
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
			def preCondResult = client.process(["-sa", "src/test/resources/Patch5401.json"])
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

	def "Patch Cli Log Patch activity in PatchLog file "() {
		setup:
			def client = PatchCli.create()
		when:
			def preCondResult = client.process(["-sa", "src/test/resources/Patch5401.json"])
		then:
			preCondResult != null
			preCondResult.returnCode == 0
			File patchFile = new File("${dbLocation}/Patch5401.json")
			patchFile.exists()
			ObjectMapper patchMapper = new ObjectMapper()
			def p = patchMapper.readValue(patchFile,Patch.class)
			p.setCurrentTarget("chei211")
			p.setCurrentPipelineTask("Build")
			p.setLogText("started")
			patchMapper.writeValue(patchFile, p)
		when:
			client.process(["-log", "5401"])
		then:
			preCondResult != null
			preCondResult.returnCode == 0
			File patchLogFile = new File("${dbLocation}/PatchLog5401.json")
			patchLogFile.exists()
			ObjectMapper patchLogMapper = new ObjectMapper()
			def pl = patchLogMapper.readValue(patchLogFile,PatchLog.class)
			assert pl.logDetails.size() == 1
		cleanup:
			repo.clean()
	}

	def "Patch Cli start startAssembleAndDeployPipeline" () {
		setup:
			def client = PatchCli.create()
		when:
			def result = client.process(["-adp", "chei212"])
		then:
			result.returnCode == 0
			result.results != null
			!result.results.isEmpty()
		cleanup:
			repo.clean()
	}

	def "Patch Cli start startInstallPipeline" () {
		setup:
			def client = PatchCli.create()
		when:
			def result = client.process(["-i", "chei212"])
		then:
			result.returnCode == 0
			result.results != null
			!result.results.isEmpty()
		cleanup:
			repo.clean()
	}

	def "Patch cli startJenkinsJob" () {
		setup:
			def client = PatchCli.create()
		when:
			def result = client.process(["-sj", "testJob"])
		then:
			result.returnCode == 0
			result.results != null
		cleanup:
			repo.clean()
	}

	def "Patch cli startJenkinsJob with string parameters" () {
		setup:
			def client = PatchCli.create()
		when:
			def result = client.process(["-sjsp", 'testJob,param1@=value1@:p2@=v2@:testParam3@=thisisThirdValue'])
		then:
			result.returnCode == 0
			result.results != null
		cleanup:
			repo.clean()
	}

	def "Patch cli startJenkinsJob with file parameters" () {
		setup:
		def client = PatchCli.create()
		when:
		def result = client.process(["-sjfp", 'testJob,param1@=src/test/resources/Patch5401.json'])
		then:
		result.returnCode == 0
		result.results != null
		cleanup:
		repo.clean()
	}

	// JHE (18.08.2020): Ignoring the test as it requires pre-requisite in DB. However, keeping it for future sanity checks
	@Ignore
	@Requires({PatchCliIntegrationTest.dbAvailable()})
	def "Patch Cli update status of Patch"() {
		when:
			def patchcli = PatchCli.create()
			def result = patchcli.process(["-dbsta", "7018,0"])
		then:
			result.returnCode == 0
			println result
	}

	// JHE (19.08.2020) : Ignoring this one because not guaranteed that Patches still exist
	@Ignore
	@Requires({PatchCliIntegrationTest.dbAvailable()})
	def "Patch Cli correctly copies Patch JSON Files"() {
		String destFolderPath = "src/test/resources/destFolderForPatch"
		when:
			new File(destFolderPath).mkdirs()
			def patchcli = PatchCli.create()
			patchcli.process(["-sa", "src/test/resources/Patch6201.json"])
			patchcli.process(["-sa", "src/test/resources/Patch6202.json"])
			def result = patchcli.process(["-cpf","Anwendertest,${System.getProperty('java.io.tmpdir')}"])
		then:
			println result
		cleanup:
			new File(destFolderPath).deleteDir()

	}

	// Preconditions for Tests used via Spock @Require
	static def dbConnection() {
		Properties pr = loadProperties()
		def dbConnection = Sql.newInstance(pr.get("db.url"), pr.get("db.user"), pr.get("db.passwd"))
		dbConnection
	}

	static def dbConnection(String user, String passwd) {
		Properties pr = loadProperties()
		def dbConnection = Sql.newInstance(pr.get("db.url"), user, passwd)
		dbConnection
	}

	static boolean dbAvailable() {
		try {
			dbConnection()
			return true
		} catch (Exception e) {
			return false
		}
	}
	static boolean patchExists(String patchNumber) {
		try {
			def id = patchNumber as Long
			def db = dbConnection()
			def sql = 'select status from  cm_patch_f where id = :patchNumber'
			def result = db.firstRow(sql, [patchNumber:id])
			return result != null
		} catch (Exception e) {
			return false
		}
	}
}
