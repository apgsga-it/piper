package com.apgsga.patch.service.client

import com.apgsga.microservice.patch.api.Patch
import com.apgsga.microservice.patch.api.PatchLog
import com.apgsga.microservice.patch.api.PatchPersistence
import com.apgsga.microservice.patch.server.MicroPatchServer
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonSlurper
import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.core.io.FileSystemResourceLoader
import org.springframework.core.io.ResourceLoader
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import spock.lang.Ignore
import spock.lang.Requires
import spock.lang.Specification

@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = [MicroPatchServer.class])
@TestPropertySource(locations = ["classpath:config/server-test.properties"])
@ActiveProfiles("test,mock,mockMavenRepo,groovyactions")
class PatchCliIntegrationTest extends Specification {

	private static final String CLASSPATH_CONFIG_OPS_TEST_PROPERTIES = 'classpath:config/ops-test.properties'

	private static final String CLASSPATH_CONFIG_APP_TEST_PROPERTIES = 'classpath:config/app-test.properties'
	
	@Value('${json.db.location}')
	private String dbLocation

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
		System.properties['appPropertiesFile'] = CLASSPATH_CONFIG_APP_TEST_PROPERTIES
		System.properties['opsPropertiesFile'] = CLASSPATH_CONFIG_OPS_TEST_PROPERTIES
		println System.getProperties()
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
	
	def "Patch Cli redo's Patch, which has been saved before with -sa"() {
		setup:
		def client = PatchCli.create()
		when:
		def preCondResult2 = client.process(["-sa", "src/test/resources/Patch5401.json"])
		def result = client.process(["-redo", "5401"])

		then:
		preCondResult2 != null
		preCondResult2.returnCode == 0
		result != null
		result.returnCode == 0
		def dbFile = new File("${dbLocation}/Patch5401.json")
		def sourceFile = new File("src/test/resources/Patch5401.json")
		ObjectMapper mapper = new ObjectMapper()
		mapper.readValue(sourceFile,Patch.class).equals(mapper.readValue(dbFile,Patch.class))
		cleanup:
		repo.clean()
	}

	def "Patch Cli removes Patch, which been copied before"() {
		setup:
		def client = PatchCli.create()
		when:
		def preCondResult = client.process(["-s", "src/test/resources/Patch5401.json"])
		def result = client.process(["-r", "5401"])
		then:
		preCondResult != null
		preCondResult.returnCode == 0
		result != null
		result.returnCode == 0
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
		// TODO JHE (19.08.2020): Before my change we were checking if returnCode was 0 ... but actually, if there is an error, we shouldn receive a 1, isn't it ???
		result.returnCode == 1
	}

    // TODO (jhe, che) : This test needs the TargetSystemMapping File on Server
	// TODO The whole TargetsystemMapping dealing needs to be cleaned up , see IT-35944
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
			client.process(["-log", "src/test/resources/Patch5401.json"])
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

	// TODO (che, jhe ) : This stay still here?
	@Requires({PatchCliIntegrationTest.dbAvailable()})
	def "Patch DB Cli returns patch ids to be re-installed after a clone"() {
		when:
		def patchcli = PatchCli.create()
		def outputFile = new File("src/test/resources/patchToBeReinstalledInformatiktest.json")
		def result = patchcli.process(["-lpac", "Informatiktest"])
		then:
		result != null
		result.returnCode == 0
		outputFile.exists()
		def patchList = new JsonSlurper().parseText(outputFile.text)
		println "content of outputfile : ${patchList}"
		cleanup:
		outputFile.delete()
	}

	// JHE (18.08.2020): Ignoring the test as it requires pre-requisite in DB. However, keeping it for future sanity checks
	@Ignore
	def "Patch DB Cli  update status of Patch"() {
		when:
			def patchcli = PatchCli.create()
			def result = patchcli.process(["-dbsta", "7018,Entwicklung"])
		then:
			println result
	}

	// JHE (19.08.2020) : Ignoring this one because not guaranteed that Patches still exist
	@Ignore
	@Requires({PatchCliIntegrationTest.dbAvailable()})
	def "Patch DB Cli correctly copies Patch JSON Files"() {
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

	static Properties loadProperties() {
		ResourceLoader rl = new FileSystemResourceLoader()
		def resource = rl.getResource(CLASSPATH_CONFIG_OPS_TEST_PROPERTIES)
		Properties properties = new Properties()
		File propertiesFile = resource.getFile()
		propertiesFile.withInputStream {
			properties.load(it)
		}
		properties
	}
}
