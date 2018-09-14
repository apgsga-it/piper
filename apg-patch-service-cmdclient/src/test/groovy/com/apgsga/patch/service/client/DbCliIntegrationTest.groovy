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

@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@TestPropertySource(locations = "application-test.properties")
class DbCliIntegrationTest extends Specification {
	
	def "Patch DB Cli should print out help without errors"() {
		def result = PatchDbCli.create().process(["-h"])
		expect: "PatchDbCli returns null in case of help only (-h)"
		result != null
		result.returnCode == 0
		result.results.size() == 0
	}
	
	def "Patch DB Cli returns patch ids to be re-installed after a clone"() {
		setup:
			def patchDbCli = Stub(PatchDbCli.class)
			def patchNumbers = [1234,5678]
			patchDbCli.listPatchAfterClone("Informatiktest") >> patchNumbers
			def result
		when:
			result = patchDbCli.process(["-lpac", "Informatiktest"])
		then:
			result != null
	}

}
