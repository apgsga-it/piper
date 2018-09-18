package com.apgsga.patch.service.client

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode
import org.springframework.test.context.TestPropertySource

import com.apgsga.patch.service.client.revision.PatchRevisionCli

import spock.lang.Specification

@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@TestPropertySource(locations = "application-test.properties")
class RevisionCliIntegrationTest extends Specification {
	
	def "Patch Cli validate retrieve and save revision"() {
		setup:
			def client = PatchRevisionCli.create()
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
			revisionAsJson = TestUtil.getRevisionLine(buffer.toString())
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
			revisionAsJson = TestUtil.getRevisionLine(buffer.toString())
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
			revisionAsJson = TestUtil.getRevisionLine(buffer.toString())
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
			revisionAsJson = TestUtil.getRevisionLine(buffer.toString())
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
			revisionAsJson = TestUtil.getRevisionLine(buffer.toString())
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
			revisionAsJson = TestUtil.getRevisionLine(buffer.toString())
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
			revisionAsJson = TestUtil.getRevisionLine(buffer.toString())
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
	
	def "Patch Cli validate retrieve last prod revision"() {
		setup:
			/*
			 * For our tests, within src/test/resources/TargetSystemMappings.json, CHEI211 is configured as the
			 * production target.
			 *
			 */
			def client = PatchRevisionCli.create()
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
			revisionAsJson = TestUtil.getLastProdRevisionLine(buffer.toString())
			revisionsFromRRCall = new JsonSlurper().parseText(revisionAsJson)
		then:
			revisionsFromRRCall.lastProdRevision.toInteger() == 4
		cleanup:
			revisionsFile.delete()
	}
	
	// TODO JHE (26.06.2018): will be removed with JAVA8MIG-389
	def "Patch Cli delete all T revision with dryRun"() {
		setup:
			def client = PatchCli.create()
		when:
			client.process(["-rtr", "1"]) // 1 -> dryRun
		then:
			// Simply nothing should happen.
			notThrown(RuntimeException)
	}
}
