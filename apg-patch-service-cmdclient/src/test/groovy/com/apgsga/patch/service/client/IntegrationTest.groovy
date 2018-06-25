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

import com.apgsga.microservice.patch.api.DbModules
import com.apgsga.microservice.patch.api.Patch
import com.apgsga.microservice.patch.api.PatchPersistence
import com.apgsga.microservice.patch.api.ServicesMetaData
import com.apgsga.microservice.patch.server.MicroPatchServer;
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import spock.lang.Ignore
import spock.lang.Specification;

@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = [MicroPatchServer.class ])
@TestPropertySource(locations = "application-test.properties")
@ActiveProfiles("test,mock,groovyactions")
public class IntegrationTest extends Specification {

	// TODO JHE: Will be deleted?
//	@Value('${baseUrl}')
//	private String baseUrl;

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
		def result = PatchCli.create("test").process(["-h"])
		expect: "PatchCli returns null in case of help only (-h)"
		result != null
		result.returnCode == 0
		result.results.size() == 0
	}

	def "Patch Cli should print out help without errors in case of no options "() {
		def result = PatchCli.create("test").process([])
		expect: "PatchCli returns null in case no options entered"
		result != null
		result.returnCode == 0
		result.results.size() == 0
	}

	def "Patch Cli queries existance of not existing Patch and returns false"() {
		setup:
		def client = PatchCli.create("test")
		when:
		def result = client.process(["-e", "9999"])
		then:
		result != null
		result.returnCode == 0
		result.results['e'].exists == false
	}

	def "Patch Cli copies Patch File to server and queries before and after existence"() {
		setup:
		def client = PatchCli.create("test")
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

	def "Patch Cli return found = false on findById of non existing Patch"() {
		setup:
		def client = PatchCli.create("test")
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
		def client = PatchCli.create("test")
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
		def client = PatchCli.create("test")
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
		def client = PatchCli.create("test")
		when:
		//def result = client.process(["-u", baseUrl, "-ud", "src/test/resources/DbModules.json"])
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
		def client = PatchCli.create("test")
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
		def client = PatchCli.create("test")
		when:
		def result = client.process(["-dd", "build"])
		then:
		result != null
		result.returnCode == 0
		result.results['dd'].exists == false
	}

	def "Patch Cli upload ServiceMetaData to server"() {
		setup:
		def client = PatchCli.create("test")
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
		def client = PatchCli.create("test")
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
		def client = PatchCli.create("test")
		when:
		def result = client.process(["-dm", "build"])
		then:
		result != null
		result.returnCode == 0
		result.results['dm'].exists == false
	}

	def "Patch Cli invalid State Change Action"() {
		setup:
		def client = PatchCli.create("test")
		when:
		def result = client.process(["-sta", "9999,XXXXXX,aps"])
		then:
		result != null
		result.returnCode == 0
	}


	def "Patch Cli valid State Change Action for config aps"() {
		setup:
		def client = PatchCli.create("test")
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
		def client = PatchCli.create("test")
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
		def client = PatchCli.create("test")
		when:
		def result = client.process(["-sta", "9999,EntwicklungInstallationsbereit"])
		then:
		result != null
		result.returnCode == 0
	}


	def "Patch Cli valid State Change Action for config db with config file"() {
		setup:
		def client = PatchCli.create("test")
		when:
		def preCondResult = client.process(["-s", "src/test/resources/Patch5401.json"])
		def result = client.process(["-sta", "5401,EntwicklungInstallationsbereit,mockdb"])
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
		def client = PatchCli.create("test")
		when:
		def result = client.process(["-vv", "9.0.6.ADMIN-UIMIG-SNAPSHOT,it21_release_9_0_6_admin_uimig"])
		then:
		result != null
		result.returnCode == 0
	}
	
	def "Patch Cli validate retrieve and save revision"() {
		setup:
			def client = PatchCli.create("test")
			PrintStream oldStream
			def buffer
			def revisionAsJson
			def revisionsFromRRCall
			def revisionsFromFile
			def revisionsFile = new File("src/test/resources/Revisions.json")
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			client.process(["-rr", "T,CHEI212"])
			System.setOut(oldStream)
			revisionAsJson = getRetrieveRevisionLine(buffer.toString())
			revisionsFromRRCall = new JsonSlurper().parseText(revisionAsJson)
		then:
			revisionsFromRRCall.fromRetrieveRevision.revision.toInteger() == 10000
			revisionsFromRRCall.fromRetrieveRevision.lastRevision == "SNAPSHOT"
			!revisionsFile.exists()
		when:
			client.process(["-sr", "T,CHEI212,${revisionsFromRRCall.fromRetrieveRevision.revision}"])
			revisionsFromFile = new JsonSlurper().parseText(revisionsFile.text)
		then: 
			revisionsFile.exists()
			revisionsFromFile.lastRevisions["CHEI212"].toInteger() == 10000
			revisionsFromFile.lastRevisions["CHEI211"] == null
			revisionsFromFile.currentRevision["P"].toInteger() == 1
			revisionsFromFile.currentRevision["T"].toInteger() == 20000
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			client.process(["-rr", "T,CHEI211"])
			System.setOut(oldStream)
			revisionAsJson = getRetrieveRevisionLine(buffer.toString())
			revisionsFromRRCall = new JsonSlurper().parseText(revisionAsJson)
			revisionsFromFile = new JsonSlurper().parseText(revisionsFile.text)
		then:
			revisionsFromRRCall.fromRetrieveRevision.revision.toInteger() == 20000
			revisionsFromRRCall.fromRetrieveRevision.lastRevision == "SNAPSHOT"
			revisionsFile.exists()
			revisionsFromFile.lastRevisions["CHEI212"].toInteger() == 10000
			revisionsFromFile.lastRevisions["CHEI211"] == null
			revisionsFromFile.currentRevision["P"].toInteger() == 1
			revisionsFromFile.currentRevision["T"].toInteger() == 20000
		when:
			client.process(["-sr", "T,CHEI211,${revisionsFromRRCall.fromRetrieveRevision.revision}"])
			revisionsFromFile = new JsonSlurper().parseText(revisionsFile.text)
		then:
			revisionsFile.exists()
			revisionsFromFile.lastRevisions["CHEI212"].toInteger() == 10000
			revisionsFromFile.lastRevisions["CHEI211"].toInteger() == 20000
			revisionsFromFile.currentRevision["P"].toInteger() == 1
			revisionsFromFile.currentRevision["T"].toInteger() == 30000
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			client.process(["-rr", "T,CHEI212"])
			System.setOut(oldStream)
			revisionAsJson = getRetrieveRevisionLine(buffer.toString())
			revisionsFromRRCall = new JsonSlurper().parseText(revisionAsJson)
		then:
			revisionsFromRRCall.fromRetrieveRevision.revision.toInteger() == 10001
			revisionsFromRRCall.fromRetrieveRevision.lastRevision.toInteger() == 10000
			revisionsFile.exists()
		when:
			client.process(["-sr", "T,CHEI212,${revisionsFromRRCall.fromRetrieveRevision.revision}"])
			revisionsFromFile = new JsonSlurper().parseText(revisionsFile.text)
		then:
			revisionsFile.exists()
			revisionsFromFile.lastRevisions["CHEI212"].toInteger() == 10001
			revisionsFromFile.lastRevisions["CHEI211"].toInteger() == 20000
			revisionsFromFile.currentRevision["P"].toInteger() == 1
			revisionsFromFile.currentRevision["T"].toInteger() == 30000
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			client.process(["-rr", "P,CHPI211"])
			System.setOut(oldStream)
			revisionAsJson = getRetrieveRevisionLine(buffer.toString())
			revisionsFromRRCall = new JsonSlurper().parseText(revisionAsJson)
		then:
			revisionsFromRRCall.fromRetrieveRevision.revision.toInteger() == 1
			revisionsFromRRCall.fromRetrieveRevision.lastRevision == "SNAPSHOT" 
			revisionsFile.exists()
		when:
			client.process(["-sr", "P,CHPI211,${revisionsFromRRCall.fromRetrieveRevision.revision}"])
			revisionsFromFile = new JsonSlurper().parseText(revisionsFile.text)
		then:
			revisionsFile.exists()
			revisionsFromFile.lastRevisions["CHEI212"].toInteger() == 10001
			revisionsFromFile.lastRevisions["CHEI211"].toInteger() == 20000
			revisionsFromFile.lastRevisions["CHPI211"].toInteger() == 1
			revisionsFromFile.currentRevision["P"].toInteger() == 2
			revisionsFromFile.currentRevision["T"].toInteger() == 30000
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			client.process(["-rr", "T,CHEI211"])
			System.setOut(oldStream)
			revisionAsJson = getRetrieveRevisionLine(buffer.toString())
			revisionsFromRRCall = new JsonSlurper().parseText(revisionAsJson)
		then:
			revisionsFromRRCall.fromRetrieveRevision.revision.toInteger() == 20001
			revisionsFromRRCall.fromRetrieveRevision.lastRevision.toInteger() == 20000
			revisionsFile.exists()
		when:
			client.process(["-sr", "T,CHEI211,${revisionsFromRRCall.fromRetrieveRevision.revision}"])
			revisionsFromFile = new JsonSlurper().parseText(revisionsFile.text)
		then:
			revisionsFile.exists()
			revisionsFromFile.lastRevisions["CHEI212"].toInteger() == 10001
			revisionsFromFile.lastRevisions["CHEI211"].toInteger() == 20001
			revisionsFromFile.lastRevisions["CHPI211"].toInteger() == 1
			revisionsFromFile.currentRevision["P"].toInteger() == 2
			revisionsFromFile.currentRevision["T"].toInteger() == 30000
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			client.process(["-rr", "P,CHPI211"])
			System.setOut(oldStream)
			revisionAsJson = getRetrieveRevisionLine(buffer.toString())
			revisionsFromRRCall = new JsonSlurper().parseText(revisionAsJson)
		then:
			revisionsFromRRCall.fromRetrieveRevision.revision.toInteger() == 2
			revisionsFromRRCall.fromRetrieveRevision.lastRevision.toInteger() == 1
			revisionsFile.exists()
		when:
			client.process(["-sr", "P,CHPI211,${revisionsFromRRCall.fromRetrieveRevision.revision}"])
			revisionsFromFile = new JsonSlurper().parseText(revisionsFile.text)
		then:
			revisionsFile.exists()
			revisionsFromFile.lastRevisions["CHEI212"].toInteger() == 10001
			revisionsFromFile.lastRevisions["CHEI211"].toInteger() == 20001
			revisionsFromFile.lastRevisions["CHPI211"].toInteger() == 2
			revisionsFromFile.currentRevision["P"].toInteger() == 3
			revisionsFromFile.currentRevision["T"].toInteger() == 30000
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			client.process(["-rr", "T,CHEI213"])
			System.setOut(oldStream)
			revisionAsJson = getRetrieveRevisionLine(buffer.toString())
			revisionsFromRRCall = new JsonSlurper().parseText(revisionAsJson)
		then:
			revisionsFromRRCall.fromRetrieveRevision.revision.toInteger() == 30000
			revisionsFromRRCall.fromRetrieveRevision.lastRevision == "SNAPSHOT"
			revisionsFile.exists()
		when:
			client.process(["-sr", "T,CHEI213,${revisionsFromRRCall.fromRetrieveRevision.revision}"])
			revisionsFromFile = new JsonSlurper().parseText(revisionsFile.text)
		then:
			revisionsFile.exists()
			revisionsFromFile.lastRevisions["CHEI212"].toInteger() == 10001
			revisionsFromFile.lastRevisions["CHEI211"].toInteger() == 20001
			revisionsFromFile.lastRevisions["CHEI213"].toInteger() == 30000
			revisionsFromFile.lastRevisions["CHPI211"].toInteger() == 2
			revisionsFromFile.currentRevision["P"].toInteger() == 3
			revisionsFromFile.currentRevision["T"].toInteger() == 40000
		cleanup:
			revisionsFile.delete()
	}
	
	def "Patch Cli validate onClone mechanism"() {
		setup:
			/*
			 * For our tests, within src/test/resources/TargetSystemMappings.json, CHEI211 is configured as the
			 * production target.
			 * 
			 */
			def client = PatchCli.create("test")
			def revisionsFile = new File("src/test/resources/Revisions.json")
			def currentRevision = [P:5,T:30000]
			def lastRevision = [CHEI212:10036,CHEI211:4,CHEI213:20025]
			def revisions = [lastRevisions:lastRevision, currentRevision:currentRevision]
			revisionsFile.write(new JsonBuilder(revisions).toPrettyString())
			def revisionsAfterClone
			def result
			def oldStream
			def buffer
			def revisionAsJson
			def revisionsFromRRCall
			def revisionsFromFile
		when:
			result = client.process(["-oc", "CHEI212"])
			revisionsAfterClone = new JsonSlurper().parseText(revisionsFile.text)
		then:
			result != null
			revisionsFile.exists()
			revisionsAfterClone.currentRevision["P"].toInteger() == 5
			revisionsAfterClone.currentRevision["T"].toInteger() == 30000
			revisionsAfterClone.lastRevisions["CHEI211"].toInteger() == 4
			revisionsAfterClone.lastRevisions["CHEI213"].toInteger() == 20025
			revisionsAfterClone.lastRevisions["CHEI212"] == "10000@P"
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			client.process(["-rr", "T,CHEI212"])
			System.setOut(oldStream)
			revisionAsJson = getRetrieveRevisionLine(buffer.toString())
			revisionsFromRRCall = new JsonSlurper().parseText(revisionAsJson)
		then:
			result != null
			revisionsFile.exists()
			revisionsFromRRCall.fromRetrieveRevision.revision.toInteger() == 10000
			revisionsFromRRCall.fromRetrieveRevision.lastRevision == "CLONED"
		when:
			client.process(["-sr", "T,CHEI212,${revisionsFromRRCall.fromRetrieveRevision.revision}"])
			revisionsFromFile = new JsonSlurper().parseText(revisionsFile.text)
		then:
			revisionsFile.exists()
			revisionsFromFile.lastRevisions["CHEI212"].toInteger() == 10000
			revisionsFromFile.lastRevisions["CHEI211"].toInteger() == 4
			revisionsFromFile.lastRevisions["CHEI213"].toInteger() == 20025
			revisionsFromFile.currentRevision["P"].toInteger() == 5
			revisionsFromFile.currentRevision["T"].toInteger() == 30000
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			client.process(["-rr", "T,CHEI212"])
			System.setOut(oldStream)
			revisionAsJson = getRetrieveRevisionLine(buffer.toString())
			revisionsFromRRCall = new JsonSlurper().parseText(revisionAsJson)
		then:
			result != null
			revisionsFile.exists()
			revisionsFromRRCall.fromRetrieveRevision.revision.toInteger() == 10001
			revisionsFromRRCall.fromRetrieveRevision.lastRevision.toInteger() == 10000
		cleanup:
			revisionsFile.delete()
	}
	
	def "Patch Cli validate retrieve last prod revision"() {
		setup:
			/*
			 * For our tests, within src/test/resources/TargetSystemMappings.json, CHEI211 is configured as the
			 * production target.
			 *
			 */
			def client = PatchCli.create("test")
			def revisionsFile = new File("src/test/resources/Revisions.json")
			def currentRevision = [P:5,T:30000]
			def lastRevision = [CHEI212:10036,CHEI211:4,CHEI213:20025]
			def revisions = [lastRevisions:lastRevision, currentRevision:currentRevision]
			revisionsFile.write(new JsonBuilder(revisions).toPrettyString())
			def oldStream
			def buffer
			def revisionAsJson
			def revisionsFromRRCall
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			client.process(["-pr"])
			System.setOut(oldStream)
			revisionAsJson = getLastProdRevisionLine(buffer.toString())
			revisionsFromRRCall = new JsonSlurper().parseText(revisionAsJson)
		then:
			revisionsFromRRCall.lastProdRevision.toInteger() == 4
		cleanup:
			revisionsFile.delete()
	}
	
	def "Patch Cli delete all T revision with dryRun"() {
		setup:
			def client = PatchCli.create("test")
		when:
			client.process(["-rtr", "1"]) // 1 -> dryRun
		then:
			// Simply nothing should happen.
			notThrown(RuntimeException)
	}
	
	def getLastProdRevisionLine(String lines) {
		// Looking for the line which is for us interesting -> should contain "fromRetrieveRevision"
		def searchedLine = null
		lines.eachLine{ line ->
			if (line != null) {
				if(line.contains("lastProdRevision")) {
					searchedLine = line
				}
			}
		}
		return searchedLine
	}
	
	def getRetrieveRevisionLine(String lines) {
		// Looking for the line which is for us interesting -> should contain "fromRetrieveRevision"
		def searchedLine = null
		lines.eachLine{ line ->
			if (line != null) {
				if(line.contains("fromRetrieveRevision")) {
					searchedLine = line
				}
			}
		}
		return searchedLine
	}
}
