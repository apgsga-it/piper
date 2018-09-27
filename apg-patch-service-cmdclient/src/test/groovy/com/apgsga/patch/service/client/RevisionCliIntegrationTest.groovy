package com.apgsga.patch.service.client

import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import org.json.JSONObject
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode
import org.springframework.test.context.TestPropertySource

import com.apgsga.patch.service.client.revision.PatchRevisionCli

import spock.lang.Ignore
import spock.lang.Specification

@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@TestPropertySource(locations = "application-test.properties")
class RevisionCliIntegrationTest extends Specification {
	
	def usageString = "usage: apsrevpli.sh -[h|ar|lr|lpr|spr|nr|rr]"
	
	@Ignore
	def "___ test"() {
		setup:
			def f = new File("src/test/resources/TEST_JHE_Revisions.json")
			def target = "chei212"
			def lastTargetRev = null

			if(f.exists()) {
				f.delete()
			}
		
			def builder = new JsonBuilder()
			
			builder {
				chei212 {
					lastRev(null)
					revs([])
				}
				lastProdRev(null)
				nextRev(1)
			}
			
			f.write(builder.toPrettyString())

			def f2 = new File("src/test/resources/TEST_JHE_2_Revisions.json")
			if(f2.exists()) {
				f2.delete()
			}

		when:

			
			def builderFromExpected = new JsonBuilder()
			
//			builder2 {
//				chei212 {
//					lastRev(null)
//					revs([])
//				}
//			}
			
			builderFromExpected {
				chei212 {
					lastRev(null)
					revs([])
				}
				lastProdRev(null)
				nextRev(1)
			}

			def s = ""
			
			
			def slurper = new JsonSlurper()
			def initJson = slurper.parse(f)
			
			initJson.each {
				s += it
				s += ","
			}
			
			println "S : ${s}"
			println JsonOutput.toJson(s)
			
			def builder2 = new JsonBuilder(JsonOutput.toJson(s))
			

//			println initJson.toString()		
//			println "===================================="
			println builder2.toPrettyString()
//			println "===================================="
//			println builderFromExpected.toPrettyString()
//			println "===================================="
//			println JsonOutput.toJson([initJson.toString(),builder2.toPrettyString()])
			
			f2.write(JsonOutput.toJson([initJson.toString(),builder2.toPrettyString()]))
			
		then:
			f.exists()
	}
	
	
	def "Patch Revision Cli validate behavior when no option has been passed"() {
		setup:
			PatchRevisionCli cli = PatchRevisionCli.create()
			PrintStream oldStream
			def buffer
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			def result = cli.process([])
			System.setOut(oldStream)
		then:
			notThrown(RuntimeException)
			result.returnCode == 0
			buffer.toString().contains(usageString)
	}
	
	def "Patch Revision Cli validate help"() {
		setup:
			PatchRevisionCli cli = PatchRevisionCli.create()
			PrintStream oldStream
			def buffer
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			def result = cli.process(["-h"])
			System.setOut(oldStream)
		then:
			notThrown(RuntimeException)
			result.returnCode == 0
			buffer.toString().contains(usageString)
	}
	
	def "Patch Revision Cli validate add revision to target without any parameter"() {
		setup:
			PatchRevisionCli cli = PatchRevisionCli.create()
			def revFile = new File("src/test/resources/Revisions.json")
			PrintStream oldStream
			def buffer
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			def result = cli.process(["-ar"])
			System.setOut(oldStream)
		then:
			!revFile.exists()
			result.returnCode == 0
			buffer.toString().contains(usageString)
			buffer.toString().toLowerCase().contains("missing argument")
	}
	
	def "Patch Revision Cli validate get next global Revision when no revision file exist"() {
		setup:
			PatchRevisionCli cli = PatchRevisionCli.create()
			def revFile = new File("src/test/resources/Revisions.json")
			PrintStream oldStream
			def buffer
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			def result = cli.process(["-nr"])
			System.setOut(oldStream)
		then:
			revFile.exists()
			result.returnCode == 0
			buffer.toString().toLong() == 1L
		cleanup:
			revFile.delete()
	}
	
	def "Patch Revision Cli validate get next global Revision"() {
		setup:
			PatchRevisionCli cli = PatchRevisionCli.create()
			def revFile = new File("src/test/resources/Revisions.json")
			PrintStream oldStream
			def buffer
			def result
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			result = cli.process(["-nr"])
			System.setOut(oldStream)
		then:
			revFile.exists()
			result.returnCode == 0
			buffer.toString().toLong() == 1L
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			result = cli.process(["-nr"])
			System.setOut(oldStream)
		then:
			revFile.exists()
			result.returnCode == 0
			buffer.toString().toLong() == 2L
		when:
			// Extra testing with a new Client -> the existing Revision file should not be overriden
			PatchRevisionCli newCli = PatchRevisionCli.create()
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			result = newCli.process(["-nr"])
			System.setOut(oldStream)
		then:
			revFile.exists()
			result.returnCode == 0
			buffer.toString().toLong() == 3L
		cleanup:
			revFile.delete()
	}
	
	def "Patch Revision Cli validate add revision to target"() {
		setup:
			PatchRevisionCli cli = PatchRevisionCli.create()
			def revFile = new File("src/test/resources/Revisions.json")
			def revAsJson
			def chei212Revisions
			def prodRevision
			def lastChei212Revision
			def nextGlobalRevision
			def result
		when:
			result = cli.process(["-ar","chei212,123"])
			revAsJson = new JsonSlurper().parse(revFile)
		then:
			revFile.exists()
			result.returnCode == 0
			revAsJson.nextRev == 1 // because we started with a brand new Revision -> although it has no meaning for this particular test
			revAsJson.prodRevision == null // because we started with a brand new Revision -> although it has no meaning for this particular test
			// TODO JHE: verify that we can access chei212 by using a variable
			revAsJson.chei212.revisions.size() == 1
			revAsJson.chei212.revisions[0].toInteger() == 123
			revAsJson.chei212.lastRevision.toInteger() == 123
		when:
			result = cli.process(["-ar","chei212,234"])
			revAsJson = new JsonSlurper().parse(revFile)
		then:
			revFile.exists()
			result.returnCode == 0
			revAsJson.nextRev == 1 // because we started with a brand new Revision -> although it has no meaning for this particular test
			revAsJson.prodRevision == null // because we started with a brand new Revision -> although it has no meaning for this particular test
			// TODO JHE: verify that we can access chei212 by using a variable
			revAsJson.chei212.revisions.size() == 2
			revAsJson.chei212.revisions.contains("123")
			revAsJson.chei212.revisions.contains("234")
			revAsJson.chei212.lastRevision.toInteger() == 234
		when:
			result = cli.process(["-ar","chti211,15"])
			revAsJson = new JsonSlurper().parse(revFile)
		then:
			revFile.exists()
			result.returnCode == 0
			revAsJson.nextRev == 1 // because we started with a brand new Revision -> although it has no meaning for this particular test
			revAsJson.prodRevision == null // because we started with a brand new Revision -> although it has no meaning for this particular test
			// TODO JHE: verify that we can access chei212 by using a variable
			revAsJson.chei212.revisions.size() == 2
			revAsJson.chei212.revisions.contains("123")
			revAsJson.chei212.revisions.contains("234")
			revAsJson.chei212.lastRevision.toInteger() == 234
			revAsJson.chti211.revisions.size() == 1
			revAsJson.chti211.revisions.contains("15")
			revAsJson.chti211.lastRevision.toInteger() == 15
		when:
			result = cli.process(["-ar","chti211,18"])
			result = cli.process(["-ar","chei212,77"])
			revAsJson = new JsonSlurper().parse(revFile)
		then:
			revFile.exists()
			result.returnCode == 0
			revAsJson.nextRev == 1 // because we started with a brand new Revision -> although it has no meaning for this particular test
			revAsJson.prodRevision == null // because we started with a brand new Revision -> although it has no meaning for this particular test
			// TODO JHE: verify that we can access chei212 by using a variable
			revAsJson.chei212.revisions.size() == 3
			revAsJson.chei212.revisions.contains("123")
			revAsJson.chei212.revisions.contains("234")
			revAsJson.chei212.revisions.contains("77")
			revAsJson.chei212.lastRevision.toInteger() == 77
			revAsJson.chti211.revisions.size() == 2
			revAsJson.chti211.revisions.contains("15")
			revAsJson.chti211.revisions.contains("18")
			revAsJson.chti211.lastRevision.toInteger() == 18
		cleanup:
			revFile.delete()
	}
	
	def "Patch Revision Cli validate get last revision for a given target"() {
		setup:
			PatchRevisionCli cli = PatchRevisionCli.create()
			PrintStream oldStream
			def buffer
			def result
			def revFile = new File("src/test/resources/Revisions.json")
			cli.process(["-ar","chti211,18"])
			cli.process(["-ar","chei212,77"])
			cli.process(["-ar","chei212,88"])
			cli.process(["-ar","chti211,185"])
			cli.process(["-ar","chei212,100"])
			cli.process(["-ar","chei211,50"])
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			result = cli.process(["-lr","chei212"])
			System.setOut(oldStream)
		then:
			revFile.exists()
			result.returnCode == 0
			buffer.toString().toLong() == 100L
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			result = cli.process(["-lr","chti211"])
			System.setOut(oldStream)
		then:
			revFile.exists()
			result.returnCode == 0
			buffer.toString().toLong() == 185L
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			result = cli.process(["-lr","chei211"])
			System.setOut(oldStream)
		then:
			revFile.exists()
			result.returnCode == 0
			buffer.toString().toLong() == 50L
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			result = cli.process(["-lr","chti215"])
			System.setOut(oldStream)
		then:
			revFile.exists()
			result.returnCode == 0
			buffer.toString().trim() == "null"
		cleanup:
			revFile.delete()
	}
	
	def "Patch Revision Cli validate set production revision"() {
		setup:
			PatchRevisionCli cli = PatchRevisionCli.create()
			def revFile = new File("src/test/resources/Revisions.json")
			def result
			def revAsJson
			
		when:
			result = cli.process(["-spr","5"])
			revAsJson = new JsonSlurper().parse(revFile)
		then:
			revAsJson.lastProdRev.toInteger() == 5
		when:
			result = cli.process(["-spr","22"])
			revAsJson = new JsonSlurper().parse(revFile)
		then:
			revAsJson.lastProdRev.toInteger() == 22
		cleanup:
			revFile.delete()
	}
	
	def "Patch Revision Cli validate get production revision"() {
		setup:
			PatchRevisionCli cli = PatchRevisionCli.create()
			def revFile = new File("src/test/resources/Revisions.json")
			def result
			PrintStream oldStream
			def buffer
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			result = cli.process(["-spr","5"])
			result = cli.process(["-pr"])
			System.setOut(oldStream)
		then:
			revFile.exists()
			result.returnCode == 0
			buffer.toString().trim() == "5"
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			result = cli.process(["-spr","50"])
			result = cli.process(["-spr","500"])
			result = cli.process(["-spr","5000"])
			result = cli.process(["-pr"])
			System.setOut(oldStream)
		then:
			revFile.exists()
			result.returnCode == 0
			buffer.toString().trim() == "5000"
		cleanup:
			revFile.delete()
	}
	
	
	def "Patch Revision Cli validate reset revision for a given target"() {
		setup:
			PatchRevisionCli cli = PatchRevisionCli.create()	
			def revFile = new File("src/test/resources/Revisions.json")
			def result
			def revAsJson
		when:
			cli.process(["-ar","chti211,18"])
			cli.process(["-ar","chei212,77"])
			cli.process(["-ar","chei212,88"])
			cli.process(["-ar","chti211,185"])
			cli.process(["-ar","chei212,100"])
			cli.process(["-ar","chei211,50"])
			cli.process(["-spr","5000"])
			cli.process(["-nr"])
			revAsJson = new JsonSlurper().parse(revFile)
		then:
			revAsJson.lastProdRev.toInteger() == 5000
			revAsJson.nextRev.toInteger() == 2
			revAsJson.chti211.revisions.size() == 2
			revAsJson.chti211.revisions.contains("18")
			revAsJson.chei212.revisions.size() == 3
			revAsJson.chei212.revisions.contains("100")
			revAsJson.chei211.revisions.size() == 1
			revAsJson.chei211.revisions.contains("50")
		when:
			result = cli.process(["-rr","chei212"])
			revAsJson = new JsonSlurper().parse(revFile)
		then:
			revAsJson.lastProdRev.toInteger() == 5000
			revAsJson.nextRev.toInteger() == 2
			revAsJson.chti211.revisions.size() == 2
			revAsJson.chti211.revisions.contains("18")
			revAsJson.chei212.revisions.size() == 0
			revAsJson.chei211.revisions.size() == 1
			revAsJson.chei211.revisions.contains("50")
		cleanup:
			revFile.delete()
	}
	
	def "Patch Revision Cli validate reset revision if no revision file exists"() {
		setup:
			assert false, "not implemented yet"
	}
	
	def "Patch Revision Cli validate reset revision if the given target is not within the revision file"() {
		setup:
			assert false, "not implemented yet"
	}
	
	
	
	
//	def "Patch Cli validate retrieve and save revision"() {
//		setup:
//			def client = PatchRevisionCli.create()
//			PrintStream oldStream
//			def buffer
//			def revisionAsJson
//			def revisionsFromRRCall
//			def revisionsFromFile
//			def revisionsFile = new File("src/test/resources/Revisions.json")
//		when:
//			oldStream = System.out;
//			buffer = new ByteArrayOutputStream()
//			System.setOut(new PrintStream(buffer))
//			client.process(["-rr", "T,CHEI212"])
//			System.setOut(oldStream)
//			revisionAsJson = TestUtil.getRevisionLine(buffer.toString())
//			revisionsFromRRCall = new JsonSlurper().parseText(revisionAsJson)
//		then:
//			revisionsFromRRCall.fromRetrieveRevision.revision.toInteger() == 10000
//			revisionsFromRRCall.fromRetrieveRevision.lastRevision == "SNAPSHOT"
//			!revisionsFile.exists()
//		when:
//			client.process(["-sr", "T,CHEI212,${revisionsFromRRCall.fromRetrieveRevision.revision}"])
//			revisionsFromFile = new JsonSlurper().parseText(revisionsFile.text)
//		then:
//			revisionsFile.exists()
//			revisionsFromFile.lastRevisions["CHEI212"].toInteger() == 10000
//			revisionsFromFile.lastRevisions["CHEI211"] == null
//			revisionsFromFile.currentRevision["P"].toInteger() == 1
//			revisionsFromFile.currentRevision["T"].toInteger() == 20000
//		when:
//			oldStream = System.out;
//			buffer = new ByteArrayOutputStream()
//			System.setOut(new PrintStream(buffer))
//			client.process(["-rr", "T,CHEI211"])
//			System.setOut(oldStream)
//			revisionAsJson = TestUtil.getRevisionLine(buffer.toString())
//			revisionsFromRRCall = new JsonSlurper().parseText(revisionAsJson)
//			revisionsFromFile = new JsonSlurper().parseText(revisionsFile.text)
//		then:
//			revisionsFromRRCall.fromRetrieveRevision.revision.toInteger() == 20000
//			revisionsFromRRCall.fromRetrieveRevision.lastRevision == "SNAPSHOT"
//			revisionsFile.exists()
//			revisionsFromFile.lastRevisions["CHEI212"].toInteger() == 10000
//			revisionsFromFile.lastRevisions["CHEI211"] == null
//			revisionsFromFile.currentRevision["P"].toInteger() == 1
//			revisionsFromFile.currentRevision["T"].toInteger() == 20000
//		when:
//			client.process(["-sr", "T,CHEI211,${revisionsFromRRCall.fromRetrieveRevision.revision}"])
//			revisionsFromFile = new JsonSlurper().parseText(revisionsFile.text)
//		then:
//			revisionsFile.exists()
//			revisionsFromFile.lastRevisions["CHEI212"].toInteger() == 10000
//			revisionsFromFile.lastRevisions["CHEI211"].toInteger() == 20000
//			revisionsFromFile.currentRevision["P"].toInteger() == 1
//			revisionsFromFile.currentRevision["T"].toInteger() == 30000
//		when:
//			oldStream = System.out;
//			buffer = new ByteArrayOutputStream()
//			System.setOut(new PrintStream(buffer))
//			client.process(["-rr", "T,CHEI212"])
//			System.setOut(oldStream)
//			revisionAsJson = TestUtil.getRevisionLine(buffer.toString())
//			revisionsFromRRCall = new JsonSlurper().parseText(revisionAsJson)
//		then:
//			revisionsFromRRCall.fromRetrieveRevision.revision.toInteger() == 10001
//			revisionsFromRRCall.fromRetrieveRevision.lastRevision.toInteger() == 10000
//			revisionsFile.exists()
//		when:
//			client.process(["-sr", "T,CHEI212,${revisionsFromRRCall.fromRetrieveRevision.revision}"])
//			revisionsFromFile = new JsonSlurper().parseText(revisionsFile.text)
//		then:
//			revisionsFile.exists()
//			revisionsFromFile.lastRevisions["CHEI212"].toInteger() == 10001
//			revisionsFromFile.lastRevisions["CHEI211"].toInteger() == 20000
//			revisionsFromFile.currentRevision["P"].toInteger() == 1
//			revisionsFromFile.currentRevision["T"].toInteger() == 30000
//		when:
//			oldStream = System.out;
//			buffer = new ByteArrayOutputStream()
//			System.setOut(new PrintStream(buffer))
//			client.process(["-rr", "P,CHPI211"])
//			System.setOut(oldStream)
//			revisionAsJson = TestUtil.getRevisionLine(buffer.toString())
//			revisionsFromRRCall = new JsonSlurper().parseText(revisionAsJson)
//		then:
//			revisionsFromRRCall.fromRetrieveRevision.revision.toInteger() == 1
//			revisionsFromRRCall.fromRetrieveRevision.lastRevision == "SNAPSHOT"
//			revisionsFile.exists()
//		when:
//			client.process(["-sr", "P,CHPI211,${revisionsFromRRCall.fromRetrieveRevision.revision}"])
//			revisionsFromFile = new JsonSlurper().parseText(revisionsFile.text)
//		then:
//			revisionsFile.exists()
//			revisionsFromFile.lastRevisions["CHEI212"].toInteger() == 10001
//			revisionsFromFile.lastRevisions["CHEI211"].toInteger() == 20000
//			revisionsFromFile.lastRevisions["CHPI211"].toInteger() == 1
//			revisionsFromFile.currentRevision["P"].toInteger() == 2
//			revisionsFromFile.currentRevision["T"].toInteger() == 30000
//		when:
//			oldStream = System.out;
//			buffer = new ByteArrayOutputStream()
//			System.setOut(new PrintStream(buffer))
//			client.process(["-rr", "T,CHEI211"])
//			System.setOut(oldStream)
//			revisionAsJson = TestUtil.getRevisionLine(buffer.toString())
//			revisionsFromRRCall = new JsonSlurper().parseText(revisionAsJson)
//		then:
//			revisionsFromRRCall.fromRetrieveRevision.revision.toInteger() == 20001
//			revisionsFromRRCall.fromRetrieveRevision.lastRevision.toInteger() == 20000
//			revisionsFile.exists()
//		when:
//			client.process(["-sr", "T,CHEI211,${revisionsFromRRCall.fromRetrieveRevision.revision}"])
//			revisionsFromFile = new JsonSlurper().parseText(revisionsFile.text)
//		then:
//			revisionsFile.exists()
//			revisionsFromFile.lastRevisions["CHEI212"].toInteger() == 10001
//			revisionsFromFile.lastRevisions["CHEI211"].toInteger() == 20001
//			revisionsFromFile.lastRevisions["CHPI211"].toInteger() == 1
//			revisionsFromFile.currentRevision["P"].toInteger() == 2
//			revisionsFromFile.currentRevision["T"].toInteger() == 30000
//		when:
//			oldStream = System.out;
//			buffer = new ByteArrayOutputStream()
//			System.setOut(new PrintStream(buffer))
//			client.process(["-rr", "P,CHPI211"])
//			System.setOut(oldStream)
//			revisionAsJson = TestUtil.getRevisionLine(buffer.toString())
//			revisionsFromRRCall = new JsonSlurper().parseText(revisionAsJson)
//		then:
//			revisionsFromRRCall.fromRetrieveRevision.revision.toInteger() == 2
//			revisionsFromRRCall.fromRetrieveRevision.lastRevision.toInteger() == 1
//			revisionsFile.exists()
//		when:
//			client.process(["-sr", "P,CHPI211,${revisionsFromRRCall.fromRetrieveRevision.revision}"])
//			revisionsFromFile = new JsonSlurper().parseText(revisionsFile.text)
//		then:
//			revisionsFile.exists()
//			revisionsFromFile.lastRevisions["CHEI212"].toInteger() == 10001
//			revisionsFromFile.lastRevisions["CHEI211"].toInteger() == 20001
//			revisionsFromFile.lastRevisions["CHPI211"].toInteger() == 2
//			revisionsFromFile.currentRevision["P"].toInteger() == 3
//			revisionsFromFile.currentRevision["T"].toInteger() == 30000
//		when:
//			oldStream = System.out;
//			buffer = new ByteArrayOutputStream()
//			System.setOut(new PrintStream(buffer))
//			client.process(["-rr", "T,CHEI213"])
//			System.setOut(oldStream)
//			revisionAsJson = TestUtil.getRevisionLine(buffer.toString())
//			revisionsFromRRCall = new JsonSlurper().parseText(revisionAsJson)
//		then:
//			revisionsFromRRCall.fromRetrieveRevision.revision.toInteger() == 30000
//			revisionsFromRRCall.fromRetrieveRevision.lastRevision == "SNAPSHOT"
//			revisionsFile.exists()
//		when:
//			client.process(["-sr", "T,CHEI213,${revisionsFromRRCall.fromRetrieveRevision.revision}"])
//			revisionsFromFile = new JsonSlurper().parseText(revisionsFile.text)
//		then:
//			revisionsFile.exists()
//			revisionsFromFile.lastRevisions["CHEI212"].toInteger() == 10001
//			revisionsFromFile.lastRevisions["CHEI211"].toInteger() == 20001
//			revisionsFromFile.lastRevisions["CHEI213"].toInteger() == 30000
//			revisionsFromFile.lastRevisions["CHPI211"].toInteger() == 2
//			revisionsFromFile.currentRevision["P"].toInteger() == 3
//			revisionsFromFile.currentRevision["T"].toInteger() == 40000
//		cleanup:
//			revisionsFile.delete()
//	}
//	
//	def "Patch Cli validate retrieve last prod revision"() {
//		setup:
//			/*
//			 * For our tests, within src/test/resources/TargetSystemMappings.json, CHEI211 is configured as the
//			 * production target.
//			 *
//			 */
//			def client = PatchRevisionCli.create()
//			def revisionsFile = new File("src/test/resources/Revisions.json")
//			def currentRevision = [P:5,T:30000]
//			def lastRevision = [CHEI212:10036,CHEI211:4,CHEI213:20025]
//			def revisions = [lastRevisions:lastRevision, currentRevision:currentRevision]
//			revisionsFile.write(new JsonBuilder(revisions).toPrettyString())
//			def oldStream
//			def buffer
//			def revisionAsJson
//			def revisionsFromRRCall
//		when:
//			oldStream = System.out;
//			buffer = new ByteArrayOutputStream()
//			System.setOut(new PrintStream(buffer))
//			client.process(["-pr"])
//			System.setOut(oldStream)
//			revisionAsJson = TestUtil.getLastProdRevisionLine(buffer.toString())
//			revisionsFromRRCall = new JsonSlurper().parseText(revisionAsJson)
//		then:
//			revisionsFromRRCall.lastProdRevision.toInteger() == 4
//		cleanup:
//			revisionsFile.delete()
//	}
//	
//	// TODO JHE (26.06.2018): will be removed with JAVA8MIG-389
//	def "Patch Cli delete all T revision with dryRun"() {
//		setup:
//			def client = PatchCli.create()
//		when:
//			client.process(["-rtr", "1"]) // 1 -> dryRun
//		then:
//			// Simply nothing should happen.
//			notThrown(RuntimeException)
//	}
}
