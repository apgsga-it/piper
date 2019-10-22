package com.apgsga.patch.service.client

import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode
import org.springframework.test.context.TestPropertySource

import com.apgsga.patch.service.client.revision.PatchRevisionCli
import com.apgsga.patch.service.client.revision.PatchRevisionClient

import groovy.json.JsonSlurper
import spock.lang.Specification

class RevisionCliIntegrationTest extends Specification {
	
	def usageString = "usage: apsrevpli.sh -[h|ar|lr|nr|rr|gr|drt|dr]"
	
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
	
	def "Patch Revision Cli validate delete Revision List for a particular target"() {
		when:
			PatchRevisionCli cli = PatchRevisionCli.create()
			def revFile = new File("src/test/resources/Revisions.json")
			cli.process(["-ar","chti211,18,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei212,77,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei212,88,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chti211,185,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei211,100,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei211,50,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei211,503,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","dev-chpi211,5000,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","dev-chpi211,6000,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","dev-chpi211,7000,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","dev-chpi211,8000,9.1.0.ADMIN-UIMIG-"])
			def result = cli.process(["-drt","chti211"])
			def oldStream = System.out;
			def buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			def grResult = cli.process(["-gr","chti211"])
		then:
			System.setOut(oldStream)
			revFile.exists()
			result.returnCode == 0
			grResult.returnCode == 0
			buffer.toString().isEmpty()
		when:
			//Check CHEI211
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			grResult = cli.process(["-gr","chei211"])
		then:
			System.setOut(oldStream)
			revFile.exists()
			grResult.returnCode == 0
			buffer.toString().toString().split(",").size() == 3
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-100")
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-50")
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-503")
		when:
			// Check CHEI212
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			grResult = cli.process(["-gr","chei212"])
		then:
			System.setOut(oldStream)
			revFile.exists()
			grResult.returnCode == 0
			buffer.toString().toString().split(",").size() == 2
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-77")
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-88")
		when:
			// Check CHPI211
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			grResult = cli.process(["-gr","dev-chpi211"])
		then:
			System.setOut(oldStream)
			revFile.exists()
			grResult.returnCode == 0
			buffer.toString().toString().split(",").size() == 4
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-5000")
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-6000")
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-7000")
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-8000")
		when:
			// Shouldn't be possible to reset the production (for test, production target is chei211)
			result = cli.process(["-drt","dev-chpi211"])
		then:
			result.returnCode == 1
		
		when:
			// Ensure nothing has been deleted for PROD target
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			grResult = cli.process(["-gr","dev-chpi211"])
		then:
			System.setOut(oldStream)
			revFile.exists()
			grResult.returnCode == 0
			buffer.toString().toString().split(",").size() == 4
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-5000")
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-6000")
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-7000")
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-8000")
		cleanup:
			revFile.delete()
	}
	
	def "Patch Revision Cli validate delete revision without any parameter"() {
		when:
			PatchRevisionCli cli = PatchRevisionCli.create()
			def revFile = new File("src/test/resources/Revisions.json")
			def oldStream = System.out;
			def buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			def result = cli.process(["-drt"])
			System.setOut(oldStream)
		then:
			!revFile.exists()
			result.returnCode == 0
			buffer.toString().contains(usageString)
			buffer.toString().toLowerCase().contains("missing argument")
	}
	
	def "Patch Revision Cli validate delete list of a given revision for a given target"() {
		when:
			PatchRevisionCli cli = PatchRevisionCli.create()
			def revFile = new File("src/test/resources/Revisions.json")
			cli.process(["-ar","chti211,18,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei212,77,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei212,88,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chti211,185,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei212,100,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei212,50,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei211,503,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chpi211,5000,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chpi211,6000,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chpi211,7000,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chpi211,8000,9.1.0.ADMIN-UIMIG-"])
			def result = cli.process(["-drt","chei212,100;50"])
			def oldStream = System.out;
			def buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			def grResult = cli.process(["-gr","chei212"])
		then:
			System.setOut(oldStream)
			revFile.exists()
			result.returnCode == 0
			grResult.returnCode == 0
			buffer.toString().toString().split(",").size() == 2
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-77")
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-88")
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			grResult = cli.process(["-gr","chti211"])
		then:
			System.setOut(oldStream)
			revFile.exists()
			grResult.returnCode == 0
			buffer.toString().toString().split(",").size() == 2
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-18")
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-185")
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			grResult = cli.process(["-gr","chpi211"])
		then:
			System.setOut(oldStream)
			revFile.exists()
			grResult.returnCode == 0
			buffer.toString().toString().split(",").size() == 4
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-5000")
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-6000")
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-7000")
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-8000")
		cleanup:
			revFile.delete()
	}
	
	def "Patch Revision Cli validate delete list of a given revision without specifing target"() {
		when:
			PatchRevisionCli cli = PatchRevisionCli.create()
			def revFile = new File("src/test/resources/Revisions.json")
			cli.process(["-ar","chti211,18,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei212,77,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei212,88,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chti211,185,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei212,100,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei212,50,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei211,503,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei211,504,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei211,505,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","chei211,706,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","dev-chpi211,5000,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","dev-chpi211,6000,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","dev-chpi211,7000,9.1.0.ADMIN-UIMIG-"])
			cli.process(["-ar","dev-chpi211,8000,9.1.0.ADMIN-UIMIG-"])
			def result = cli.process(["-dr","18;77;100;7000;8000;503;706"])
			def oldStream = System.out;
			def buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			def grResult = cli.process(["-gr","chei212"])
		then:
			System.setOut(oldStream)
			revFile.exists()
			result.returnCode == 0
			grResult.returnCode == 0
			buffer.toString().toString().split(",").size() == 2
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-88")
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-50")
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			grResult = cli.process(["-gr","chti211"])
		then:
			System.setOut(oldStream)
			revFile.exists()
			grResult.returnCode == 0
			buffer.toString().toString().split(",").size() == 1
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-185")
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			grResult = cli.process(["-gr","dev-chpi211"])
		then:
			System.setOut(oldStream)
			revFile.exists()
			grResult.returnCode == 0
			buffer.toString().toString().split(",").size() == 4
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-5000")
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-6000")
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-7000")
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-8000")
		when:
			oldStream = System.out;
			buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			grResult = cli.process(["-gr","chei211"])
		then:
			System.setOut(oldStream)
			revFile.exists()
			grResult.returnCode == 0
			buffer.toString().toString().split(",").size() == 2
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-504")
			buffer.toString().toString().contains("9.1.0.ADMIN-UIMIG-505")
		cleanup:
			revFile.delete()
	}
	
}