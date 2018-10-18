package com.apgsga.patch.service.client

import static com.apgsga.patch.service.client.DbCliIntegrationTest.dbAvailable
import static com.apgsga.patch.service.client.DbCliIntegrationTest.patchExists

import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode
import org.springframework.test.context.TestPropertySource

import com.apgsga.patch.service.client.db.PatchDbCli

import groovy.json.JsonSlurper
import groovy.sql.Sql
import spock.lang.Ignore
import spock.lang.Requires
import spock.lang.Specification


class DbCliIntegrationTest extends Specification {
	
	def setup() {
		def buildFolder = new File("build")
		if (!buildFolder.exists()) {
			def created = buildFolder.mkdir()
			println ("Buildfolder has been created ${created}")
		}
		System.properties['appPropertiesFile'] = 'classpath:config/app-test.properties'
		System.properties['opsPropertiesFile'] = 'classpath:config/ops-test.properties'
	}

	def "Patch DB Cli should print out help without errors"() {
		def result = PatchDbCli.create().process(["-h"])
		expect: "PatchDbCli returns null in case of help only (-h)"
		result != null
		result.returnCode == 0
	}
	
	@Requires({dbAvailable()})
	def "Patch DB Cli returns patch ids to be re-installed after a clone"() {
		setup:
		def patchDbCli = PatchDbCli.create()
		def result
		def outputFile = new File("src/test/resources/patchToBeReinstalled.json")
		when:
		result = patchDbCli.process(["-lpac", "Informatiktest"])
		then:
		result != null
		result.returnCode == 0
		outputFile.exists()
		def patchList = new JsonSlurper().parseText(outputFile.text)
		println "content of outputfile : ${patchList}"
		cleanup:
		outputFile.delete()
	}
	
	
	@Requires({patchExists("5799")})
	def "Patch DB Cli  update status of Patch"() {
		setup:
		def db = dbConnection("cm", "cm_pass");
		def dbResult = db.execute('update cm_patch_t set status = 1 where id = :id',["id":5799])
		def patchDbCli = PatchDbCli.create()
		def result
		def savedOut
		def buffer
		when:
		savedOut = System.out;
		buffer = new ByteArrayOutputStream()
		System.setOut(new PrintStream(buffer))
		result = patchDbCli.process(["-sta", "5799,EntwicklungInstallationsbereit"])
		System.setOut(savedOut)
		then:
		result != null
		result.returnCode == 0
		result.dbResult == false
		buffer.toString().trim() == "true"

	}
	

	// Preconditions for Tests used via Spack @Require
	static def dbConnection() {
		def jdbcConfigFile = new File("src/test/resources/config/ops.groovy")
		def defaultJdbcConfig = new ConfigSlurper().parse(jdbcConfigFile.toURI().toURL())
		def dbConnection = Sql.newInstance(defaultJdbcConfig.db.url, defaultJdbcConfig.db.user, defaultJdbcConfig.db.passwd)
		dbConnection
	}
	
	static def dbConnection(String user, String passwd) {
		def jdbcConfigFile = new File("src/test/resources/config/ops.groovy")
		def defaultJdbcConfig = new ConfigSlurper().parse(jdbcConfigFile.toURI().toURL())
		def dbConnection = Sql.newInstance(defaultJdbcConfig.db.url, user, passwd)
		dbConnection
	}

	public static boolean dbAvailable() {
		try {
			dbConnection()
			return true;
		} catch (Exception e) {
			return false
		}
	}
	public static boolean patchExists(String patchNumber) {
		try {
			def id = patchNumber as Long
			def db = dbConnection()
			def sql = 'select status from  cm_patch_f where id = :patchNumber';
			def result = db.firstRow(sql, [patchNumber:id])
			return result != null;
		} catch (Exception e) {
			return false
		}
	}
	

}
