package com.apgsga.patch.service.client

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

import com.apgsga.microservice.patch.server.MicroPatchServer
import com.apgsga.patch.service.client.db.PatchDbCli

import groovy.json.JsonSlurper
import spock.lang.Ignore
import spock.lang.Specification

// TODO JHE: to be verified, probably not all anntations are required here ...
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
//@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = [MicroPatchServer.class ])
@TestPropertySource(locations = "application-test.properties")
@ActiveProfiles("test,mock,groovyactions")
class DbCliIntegrationTest extends Specification {
	
	def "Patch DB Cli should print out help without errors"() {
		def result = PatchDbCli.create().process(["-h"])
		expect: "PatchDbCli returns null in case of help only (-h)"
		result != null
		result.returnCode == 0
		result.results.size() == 0
	}
	
	@Ignore("TODO address db preconditions")
	def "Patch DB Cli returns patch ids to be re-installed after a clone"() {
		setup:
			def patchDbCli = PatchDbCli.create()
			def result
			def outputFile = new File("src/test/resources/patchToBeReinstalled.json")
		when:
			result = patchDbCli.process(["-lpac", "Informatiktest"])
		then:
			result != null
			outputFile.exists()
			def patchList = new JsonSlurper().parseText(outputFile.text)
			println "content of outputfile : ${patchList}"
		cleanup:
			outputFile.delete()
	}

}
