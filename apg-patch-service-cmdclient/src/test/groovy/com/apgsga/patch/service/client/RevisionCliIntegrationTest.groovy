package com.apgsga.patch.service.client

import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode
import org.springframework.test.context.TestPropertySource

import com.apgsga.patch.service.client.revision.PatchRevisionCli

import groovy.json.JsonSlurper
import spock.lang.Specification

@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@TestPropertySource(locations = "application-test.properties")
class RevisionCliIntegrationTest extends Specification {
	
	def usageString = "usage: apsrevpli.sh -[h|ar|lr|lpr|spr|nr|rr]"
	
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
			buffer.toString().trim() == "SNAPSHOT"
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
	
	def "Patch Revision Cli validate reset revision if the given target is not within the revision file"() {
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
			result = cli.process(["-rr","chti215"])
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
		cleanup:
			revFile.delete()
	}
	
	def "Patch Revision Client validate get list of installed revisions for non existing target"() {
		setup:
			PatchRevisionCli cli = PatchRevisionCli.create()
			def revFile = new File("src/test/resources/Revisions.json")
			def result
			PrintStream oldStream
			def buffer
			def revisions
		when:
			cli.process(["-ar","chti211,18"])
			cli.process(["-ar","chei212,77"])
			cli.process(["-ar","chei212,88"])
			cli.process(["-ar","chti211,185"])
			cli.process(["-ar","chei212,100"])
			cli.process(["-ar","chei211,50"])
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			result = cli.process(["-llr","chei215"])
			System.setOut(oldStream)
		then:
			result.returnCode == 0
			buffer.toString().trim() == ''
		cleanup:
			revFile.delete()
	}
	
	def "Patch Revision Client validate get list of installed revisions for existing target"() {
		setup:
			PatchRevisionCli cli = PatchRevisionCli.create()
			def revFile = new File("src/test/resources/Revisions.json")
			def result
			PrintStream oldStream
			def buffer
			def revisions
		when:
			cli.process(["-ar","chti211,18"])
			cli.process(["-ar","chei212,77"])
			cli.process(["-ar","chei212,88"])
			cli.process(["-ar","chti211,185"])
			cli.process(["-ar","chei212,100"])
			cli.process(["-ar","chei211,50"])
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			result = cli.process(["-llr","chei212"])
			System.setOut(oldStream)
			revisions = buffer.toString().trim() 
		then:
			result.returnCode == 0
			revisions.contains("77")
			revisions.contains("88")
			revisions.contains("100")
		cleanup:
			revFile.delete()
	}
}