package com.apgsga.patch.service.client

import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode
import org.springframework.test.context.TestPropertySource

import com.apgsga.patch.service.client.revision.PatchRevisionCli
import com.apgsga.patch.service.client.revision.PatchRevisionClient

import groovy.json.JsonSlurper
import spock.lang.Specification

class RevisionCliIntegrationTest extends Specification {
	
	def usageString = "usage: apsrevpli.sh -[h|ar|lr|nr|rr]"
	
	def setup() {
		def buildFolder = new File("build")
		if (!buildFolder.exists()) {
			def created = buildFolder.mkdir()
			println ("Buildfolder has been created ${created}")
		}
		System.properties['spring_profiles_active'] = 'default'
		System.properties['appPropertiesFile'] = 'classpath:config/app-test.properties'
		
	}
	
	def "Patch Revision Cli validate behavior when no option has been passed"() {
		when:
			PatchRevisionCli cli = PatchRevisionCli.create()
			PrintStream oldStream = System.out;
			def buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			def result = cli.process([])
			System.setOut(oldStream)
		then:
			notThrown(RuntimeException)
			result.returnCode == 0
			buffer.toString().contains(usageString)
	}
	
	def "Patch Revision Cli validate help"() {
		when:
			PatchRevisionCli cli = PatchRevisionCli.create()
			PrintStream oldStream = System.out;
			def buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			def result = cli.process(["-h"])
			System.setOut(oldStream)
		then:
			notThrown(RuntimeException)
			result.returnCode == 0
			buffer.toString().contains(usageString)
	}
	
	def "Patch Revision Cli validate add revision to target without any parameter"() {
		when:
			PatchRevisionCli cli = PatchRevisionCli.create()
			def revFile = new File("src/test/resources/Revisions.json")
			def oldStream = System.out;
			def buffer = new ByteArrayOutputStream()
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
		when:
			PatchRevisionCli cli = PatchRevisionCli.create()
			def revFile = new File("src/test/resources/Revisions.json")
			def oldStream = System.out;
			def buffer = new ByteArrayOutputStream()
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
		when:
			PatchRevisionCli cli = PatchRevisionCli.create()
			def revFile = new File("src/test/resources/Revisions.json")
			def oldStream = System.out;
			def buffer = new ByteArrayOutputStream()
			def printStream = new PrintStream(buffer)
			System.setOut(printStream)
			def result = cli.process(["-nr"])
			printStream.flush()
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
		when:
			PatchRevisionCli cli = PatchRevisionCli.create()
			def revFile = new File("src/test/resources/Revisions.json")
			def chei212Revisions
			def prodRevision
			def lastChei212Revision
			def nextGlobalRevision
			def result = cli.process(["-ar","chei212,123,9.1.0.ADMIN-UIMIG-"])
			def revAsJson = new JsonSlurper().parse(revFile)
		then:
			revFile.exists()
			result.returnCode == 0
			revAsJson.nextRev == 1 // because we started with a brand new Revision -> although it has no meaning for this particular test
			revAsJson.prodRevision == null // because we started with a brand new Revision -> although it has no meaning for this particular test
			// TODO JHE: verify that we can access chei212 by using a variable
			revAsJson.CHEI212.revisions.size() == 1
			revAsJson.CHEI212.revisions[0].toString() == "9.1.0.ADMIN-UIMIG-123"
			revAsJson.CHEI212.lastRevision.toString() == "123"
		when:
			result = cli.process(["-ar","chei212,234,9.1.0.ADMIN-UIMIG-"])
			revAsJson = new JsonSlurper().parse(revFile)
		then:
			revFile.exists()
			result.returnCode == 0
			revAsJson.nextRev == 1 // because we started with a brand new Revision -> although it has no meaning for this particular test
			revAsJson.prodRevision == null // because we started with a brand new Revision -> although it has no meaning for this particular test
			// TODO JHE: verify that we can access chei212 by using a variable
			revAsJson.CHEI212.revisions.size() == 2
			revAsJson.CHEI212.revisions.contains("9.1.0.ADMIN-UIMIG-123")
			revAsJson.CHEI212.revisions.contains("9.1.0.ADMIN-UIMIG-234")
			revAsJson.CHEI212.lastRevision.toString() == "234"
		when:
			result = cli.process(["-ar","chti211,15,9.1.0.ADMIN-UIMIG-"])
			revAsJson = new JsonSlurper().parse(revFile)
		then:
			revFile.exists()
			result.returnCode == 0
			revAsJson.nextRev == 1 // because we started with a brand new Revision -> although it has no meaning for this particular test
			revAsJson.prodRevision == null // because we started with a brand new Revision -> although it has no meaning for this particular test
			// TODO JHE: verify that we can access chei212 by using a variable
			revAsJson.CHEI212.revisions.size() == 2
			revAsJson.CHEI212.revisions.contains("9.1.0.ADMIN-UIMIG-123")
			revAsJson.CHEI212.revisions.contains("9.1.0.ADMIN-UIMIG-234")
			revAsJson.CHEI212.lastRevision.toString() == "234"
			revAsJson.CHTI211.revisions.size() == 1
			revAsJson.CHTI211.revisions.contains("9.1.0.ADMIN-UIMIG-15")
			revAsJson.CHTI211.lastRevision.toString() == "15"
		when:
			result = cli.process(["-ar","chti211,18,9.1.0.ADMIN-UIMIG-"])
			result = cli.process(["-ar","chei212,77,9.1.0.ADMIN-UIMIG-"])
			revAsJson = new JsonSlurper().parse(revFile)
		then:
			revFile.exists()
			result.returnCode == 0
			revAsJson.nextRev == 1 // because we started with a brand new Revision -> although it has no meaning for this particular test
			revAsJson.prodRevision == null // because we started with a brand new Revision -> although it has no meaning for this particular test
			// TODO JHE: verify that we can access chei212 by using a variable
			revAsJson.CHEI212.revisions.size() == 3
			revAsJson.CHEI212.revisions.contains("9.1.0.ADMIN-UIMIG-123")
			revAsJson.CHEI212.revisions.contains("9.1.0.ADMIN-UIMIG-234")
			revAsJson.CHEI212.revisions.contains("9.1.0.ADMIN-UIMIG-77")
			revAsJson.CHEI212.lastRevision.toString() == "77"
			revAsJson.CHTI211.revisions.size() == 2
			revAsJson.CHTI211.revisions.contains("9.1.0.ADMIN-UIMIG-15")
			revAsJson.CHTI211.revisions.contains("9.1.0.ADMIN-UIMIG-18")
			revAsJson.CHTI211.lastRevision.toString() == "18"
		cleanup:
			revFile.delete()
	}
	
	def "Patch Revision Cli validate get last revision for a given target"() {
		when:
			PatchRevisionCli cli = PatchRevisionCli.create()
			def revFile = new File("src/test/resources/Revisions.json")
			cli.process(["-ar","chti211,18,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei212,77,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei212,88,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chti211,185,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei212,100,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei211,50,9.1.0.ADMIN-UIMIG-"])
			def oldStream = System.out;
			def buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			def result = cli.process(["-lr","chei212"])
			System.setOut(oldStream)
		then:
			revFile.exists()
			result.returnCode == 0
			buffer.toString().toString().trim() == "100"
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			result = cli.process(["-lr","chti211"])
			System.setOut(oldStream)
		then:
			revFile.exists()
			result.returnCode == 0
			buffer.toString().toString().trim() == "185"
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			result = cli.process(["-lr","chei211"])
			System.setOut(oldStream)
		then:
			revFile.exists()
			result.returnCode == 0
			buffer.toString().toString().trim() == "50"
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
	
	def "Patch Revision Cli validate reset revision for a given target"() {
		when:
			PatchRevisionCli cli = PatchRevisionCli.create()	
			def revFile = new File("src/test/resources/Revisions.json")
			def result
			cli.process(["-ar","chti211,18,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei212,77,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei212,88,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chti211,185,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei212,100,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei211,50,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chpi211,5000,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-nr"])
			def revAsJson = new JsonSlurper().parse(revFile)
		then:
			revAsJson.nextRev.toInteger() == 2
			revAsJson.CHTI211.revisions.size() == 2
			revAsJson.CHTI211.revisions.contains("9.1.0.ADMIN-UIMIG-18")
			revAsJson.CHEI212.revisions.size() == 3
			revAsJson.CHEI212.revisions.contains("9.1.0.ADMIN-UIMIG-100")
			revAsJson.CHEI211.revisions.size() == 1
			revAsJson.CHEI211.revisions.contains("9.1.0.ADMIN-UIMIG-50")
			revAsJson.CHPI211.revisions.size() == 1
			revAsJson.CHPI211.revisions.contains("9.1.0.ADMIN-UIMIG-5000")
		when:
			result = cli.process(["-rr","chpi211,chei212"])
			revAsJson = new JsonSlurper().parse(revFile)
		then:
			revAsJson.nextRev.toInteger() == 2
			revAsJson.CHTI211.revisions.size() == 2
			revAsJson.CHTI211.revisions.contains("9.1.0.ADMIN-UIMIG-18")
			revAsJson.CHEI212.revisions.size() == 0
			revAsJson.CHEI212.lastRevision == "5000"
			revAsJson.CHEI211.revisions.size() == 1
			revAsJson.CHEI211.revisions.contains("9.1.0.ADMIN-UIMIG-50")
			revAsJson.CHPI211.revisions.size() == 1
			revAsJson.CHPI211.revisions.contains("9.1.0.ADMIN-UIMIG-5000")
		cleanup:
			revFile.delete()
	}
	
	def "Patch Revision Cli validate reset revision if the given target or source is not within the revision file"() {
		when:
			PatchRevisionCli cli = PatchRevisionCli.create()
			def revFile = new File("src/test/resources/Revisions.json")
			def result
			cli.process(["-ar","chti211,18,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei212,77,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei212,88,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chti211,185,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei212,100,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei211,50,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chpi211,5000,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-nr"])
			def revAsJson = new JsonSlurper().parse(revFile)
		then:
			revAsJson.nextRev.toInteger() == 2
			revAsJson.CHTI211.revisions.size() == 2
			revAsJson.CHTI211.revisions.contains("9.1.0.ADMIN-UIMIG-18")
			revAsJson.CHEI212.revisions.size() == 3
			revAsJson.CHEI212.revisions.contains("9.1.0.ADMIN-UIMIG-100")
			revAsJson.CHEI211.revisions.size() == 1
			revAsJson.CHEI211.revisions.contains("9.1.0.ADMIN-UIMIG-50")
			revAsJson.CHPI211.revisions.size() == 1
			revAsJson.CHPI211.revisions.contains("9.1.0.ADMIN-UIMIG-5000")
		when:
			result = cli.process(["-rr","chpi211,chti215"])
			revAsJson = new JsonSlurper().parse(revFile)
		then:
			revAsJson.nextRev.toInteger() == 2
			revAsJson.CHTI211.revisions.size() == 2
			revAsJson.CHTI211.revisions.contains("9.1.0.ADMIN-UIMIG-18")
			revAsJson.CHEI212.revisions.size() == 3
			revAsJson.CHEI212.revisions.contains("9.1.0.ADMIN-UIMIG-100")
			revAsJson.CHEI211.revisions.size() == 1
			revAsJson.CHEI211.revisions.contains("9.1.0.ADMIN-UIMIG-50")
			revAsJson.CHPI211.revisions.size() == 1
			revAsJson.CHPI211.revisions.contains("9.1.0.ADMIN-UIMIG-5000")
		when:
			result = cli.process(["-rr","chti215,chei212"])
			revAsJson = new JsonSlurper().parse(revFile)
		then:
			revAsJson.nextRev.toInteger() == 2
			revAsJson.CHTI211.revisions.size() == 2
			revAsJson.CHTI211.revisions.contains("9.1.0.ADMIN-UIMIG-18")
			revAsJson.CHEI212 == null
			revAsJson.CHEI211.revisions.size() == 1
			revAsJson.CHEI211.revisions.contains("9.1.0.ADMIN-UIMIG-50")
			revAsJson.CHPI211.revisions.size() == 1
			revAsJson.CHPI211.revisions.contains("9.1.0.ADMIN-UIMIG-5000")
		cleanup:
			revFile.delete()
	}
	
	def "Patch Revision Cli validate get List of Revision for a particular target"() {
		when:
			PatchRevisionCli cli = PatchRevisionCli.create()
			def revFile = new File("src/test/resources/Revisions.json")
			cli.process(["-ar","chti211,18,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei212,77,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei212,88,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chti211,185,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei212,100,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei211,50,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chpi211,5000,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chpi211,6000,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chpi211,7000,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chpi211,8000,9.1.0.ADMIN-UIMIG-"])
			def oldStream = System.out;
			def buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			def result = cli.process(["-gr","chpi211"])
		then:
			System.setOut(oldStream)
			revFile.exists()
			result.returnCode == 0
			buffer.toString().toString().split(",").size() == 4
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-5000")
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-6000")
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-7000")
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-8000")
		cleanup:
			revFile.delete()
	}
}