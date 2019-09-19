package com.apgsga.patch.service.client

import static com.apgsga.patch.service.client.DbCliIntegrationTest.dbAvailable
import static com.apgsga.patch.service.client.DbCliIntegrationTest.patchExists

import org.springframework.core.io.FileSystemResourceLoader
import org.springframework.core.io.ResourceLoader
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode
import org.springframework.test.context.TestPropertySource

import com.apgsga.patch.service.client.db.PatchDbCli
import com.apgsga.patch.service.client.utils.AppContext

import groovy.json.JsonSlurper
import groovy.sql.Sql
import spock.lang.Ignore
import spock.lang.Requires
import spock.lang.Specification


class DbCliIntegrationTest extends Specification {
	
	//test commit in a branch linked to a closed pull request ...

	private static final String CLASSPATH_CONFIG_OPS_TEST_PROPERTIES = 'classpath:config/ops-test.properties'

	private static final String CLASSPATH_CONFIG_APP_TEST_PROPERTIES = 'classpath:config/app-test.properties'
	
	def setup() {
		def buildFolder = new File("build")
		if (!buildFolder.exists()) {
			def created = buildFolder.mkdir()
			println ("Buildfolder has been created ${created}")
		}
		System.setProperty("spring.profiles.active","dbcli")
		System.setProperty("appPropertiesFile",CLASSPATH_CONFIG_APP_TEST_PROPERTIES)
		System.setProperty("opsPropertiesFile",CLASSPATH_CONFIG_OPS_TEST_PROPERTIES)
		println System.getProperties()
	}

	def "Patch DB Cli should print out help without errors"() {
		def result = PatchDbCli.create().process(["-h"])
		expect: "PatchDbCli returns null in case of help only (-h)"
		result != null
		result.returnCode == 0
	}
	

	
	@Requires({dbAvailable()})
	def "Patch DB Cli returns patch ids to be re-installed after a clone"() {
		when:
		def patchDbCli = PatchDbCli.create()
		def outputFile = new File("src/test/resources/patchToBeReinstalledInformatiktest.json")
		def result = patchDbCli.process(["-lpac", "Informatiktest"])
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
		when:
		def db = dbConnection("cm", "cm_pass");
		def dbResult = db.execute('update cm_patch_t set status = 1 where id = :id',["id":5799])
		def patchDbCli = PatchDbCli.create()
		def savedOut = System.out;
		def buffer = new ByteArrayOutputStream()
		System.setOut(new PrintStream(buffer))
		def result = patchDbCli.process(["-sta", "5799,EntwicklungInstallationsbereit"])
		System.setOut(savedOut)
		then:
		result != null
		result.returnCode == 0
		result.dbResult == false
		buffer.toString().trim() == "true"

	}
	

	// Preconditions for Tests used via Spack @Require
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
	
	public static Properties loadProperties() {
		ResourceLoader rl = new FileSystemResourceLoader();
		def resource = rl.getResource(CLASSPATH_CONFIG_OPS_TEST_PROPERTIES)
		Properties properties = new Properties()
		File propertiesFile = resource.getFile()
		propertiesFile.withInputStream {
			properties.load(it)
		}
		properties
	}
	

}
