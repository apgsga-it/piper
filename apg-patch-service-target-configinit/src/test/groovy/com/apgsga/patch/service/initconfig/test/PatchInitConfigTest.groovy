package com.apgsga.patch.service.initconfig.test

import java.nio.file.Files
import java.nio.file.Paths

import javax.imageio.ImageIO.ContainsFilter

import org.apache.commons.io.FileUtils
import org.spockframework.util.Assert

import com.apgsga.patch.service.bootstrap.config.PatchInitConfigCli
import com.apgsga.patch.service.configinit.util.ConfigInitUtil

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import spock.lang.Specification

class PatchInitConfigTest extends Specification {
	
	def usageString = "usage: patchinitcli.sh -[h|i|]"
	
	def etcOptPath = "src/test/resources/etc/opt"	
	
	def targetSystemMappingFileName = "${etcOptPath}/apg-patch-common/TargetSystemMappings.json"
	
	def targetSystemMappingBackupFileName = "${targetSystemMappingFileName}.backup"
	
	def patchCliApplicationPropertiesFileName = "${etcOptPath}/apg-patch-cli/application.properties"
	
	def patchCliApplicationPropertiesBackupFileName = "${patchCliApplicationPropertiesFileName}.backup"
	
	def patchCliOpsPropertiesFileName = "${etcOptPath}/apg-patch-cli/ops.properties"
	
	def patchCliOpsPropertiesBackupFileName = "${patchCliOpsPropertiesFileName}.backup"
	
	def patchServerApplicationPropertiesFileName = "${etcOptPath}/apg-patch-service-server/application.properties"
	
	def patchServerApplicationPropertiesBackupFileName = "${patchServerApplicationPropertiesFileName}.backup"
	
	def patchServerOpsPropertiesFileName = "${etcOptPath}/apg-patch-service-server/ops.properties"
	
	def patchServerOpsPropertiesBackupFileName = "${patchServerOpsPropertiesFileName}.backup"
	
	def targetSystemMappingOriContent
	
	def ConfigObject patchCliOriApplicationPropsOriContent
	
	def ConfigObject patchCliOpsPropsOriContent
	
	def ConfigObject patchServerApplicationPropsOriContent
	
	def ConfigObject patchServerOpsPropsOriContent
	
	def setup() {
		keepCopyOfOriginalTestFiles()
	}
	
	def cleanup() {
		cleanAllBackupFiles()
		restoreContentOfOriginalTestFiles()
	}
	
	def "PatchInitConfig validate behavior when no option has been passed"() {
		when:
			PatchInitConfigCli cli = PatchInitConfigCli.create()
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
	
	def "PatchInitConfig validate help"() {
		when:
			PatchInitConfigCli cli = PatchInitConfigCli.create()
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
	
	def "PatchInitConfig validate init without providing initConfigFile"() {
		when:
			PatchInitConfigCli cli = PatchInitConfigCli.create()
			PrintStream oldStream = System.out;
			def buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			def result = cli.process(["-i"])
			System.setOut(oldStream)
		then:
			notThrown(Exception)
			result.returnCode == 0
			buffer.toString().contains(usageString)
	}
	
	def "PatchInitConfig validate init without correct initConfigFile"() {
		when:
			PatchInitConfigCli cli = PatchInitConfigCli.create()
			def result = cli.process(["-i","aWrongfile"])
		then:
			// TODO JHE: Test that we catch a meaningfull exception ... maybe with a clearer message than default exception
			notThrown(Exception)
			result.returnCode == 1
	}
	
	def "PatchInitConfig validate init did the job for TargetSystemMapping File"() {
		when:
			PatchInitConfigCli cli = PatchInitConfigCli.create()
			def targetSystemMappingFile = new File(targetSystemMappingFileName)
			def result = cli.process(["-i","src/test/resources/initconfig.properties"])
		then:
			result.returnCode == 0
			def targetSystemMappingBackupFile = new File(targetSystemMappingBackupFileName)
			targetSystemMappingBackupFile.exists()
			targetSystemMappingFile.exists()
			
			// Validate content of backup file
			def targetSystemMappingBackupFileContent = new JsonSlurper().parse(targetSystemMappingBackupFile)
			targetSystemMappingBackupFileContent.targetSystems.each({targetSystem -> 
				switch(targetSystem.name) {
					case "Entwicklung":
						Assert.that(targetSystem.target.equals("CHEI212"), "Target for Entwicklung should be CHEI212!")
						break
					case "Informatiktest":
						Assert.that(targetSystem.target.equals("CHTI211"), "Target for Informatiktest should be CHTI211!")
						break
					case "Produktion":
						Assert.that(targetSystem.target.equals("CHPI211"), "Target for Produktion should be CHPI211!")
						break
					default:
						Assert.fail("Default case is invalid, all target system should be known!")
						break
				}
			})
			targetSystemMappingBackupFileContent.otherTargetInstances.size == 4
			targetSystemMappingBackupFileContent.otherTargetInstances.contains("CHEI211")
			targetSystemMappingBackupFileContent.otherTargetInstances.contains("CHTI212")
			targetSystemMappingBackupFileContent.otherTargetInstances.contains("CHTI213")
			targetSystemMappingBackupFileContent.otherTargetInstances.contains("dev-uge.light")
						
			// validate that content of newly saved file has been adapted accordingly
			def targetSystemMappingFileContent = new JsonSlurper().parse(targetSystemMappingFile)
			targetSystemMappingFileContent.targetSystems.each({targetSystem -> 
				switch(targetSystem.name) {
					case "Entwicklung":
						Assert.that(targetSystem.target.equals("CHEI212"), "New target for Entwicklung should be CHEI212!")
						break
					case "Informatiktest":
						Assert.that(targetSystem.target.equals("CHEI211"), "New target for Informatiktest should be CHEI211!")
						break
					case "Produktion":
						Assert.that(targetSystem.target.equals("CHEI212"), "New target for Produktion should be CHEI212!")
						break
					default:
						Assert.fail("Default case is invalid, all target system should be known!")
						break
				}
			})
			targetSystemMappingFileContent.otherTargetInstances.size == 3
			targetSystemMappingBackupFileContent.otherTargetInstances.contains("CHEI211")
			targetSystemMappingBackupFileContent.otherTargetInstances.contains("CHTI212")
			targetSystemMappingBackupFileContent.otherTargetInstances.contains("CHTI213")
	}
	
	def "PatchInitConfig validate init did the job for all patch Service *.properties files"() {
		when:
			PatchInitConfigCli cli = PatchInitConfigCli.create()
			def cliApplicationPropertiesFile = new File(patchCliApplicationPropertiesFileName)
			def cliApplicationPropertiesBackupFile = new File(patchCliApplicationPropertiesBackupFileName)
			def cliOpsPropertiesFile = new File(patchCliOpsPropertiesFileName)
			def cliOpsPropertiesBackupFile = new File(patchCliOpsPropertiesBackupFileName)
			def serverApplicationPropertiesFile = new File(patchServerApplicationPropertiesFileName)
			def serverApplicationPropertiesBackupFile = new File(patchServerApplicationPropertiesBackupFileName)
			def serveropsPropertiesFile = new File(patchServerOpsPropertiesFileName)
			def serveropsPropertiesBackupFile = new File(patchServerOpsPropertiesBackupFileName)
			def result = cli.process(["-i","src/test/resources/initconfig.properties"])
		then:
			result.returnCode == 0
			cliApplicationPropertiesFile.exists()
			cliApplicationPropertiesBackupFile.exists()
			cliOpsPropertiesFile.exists()
			cliOpsPropertiesBackupFile.exists()
			serverApplicationPropertiesFile.exists()
			serverApplicationPropertiesBackupFile.exists()
			serveropsPropertiesFile.exists()
			serveropsPropertiesBackupFile.exists()
		
			// Some files shouldn't have change at all
			FileUtils.contentEquals(cliApplicationPropertiesFile, cliApplicationPropertiesBackupFile)
			FileUtils.contentEquals(serveropsPropertiesFile, serveropsPropertiesBackupFile)
			
			// apg-patch-cli ops.properties + backup	
			def cliOpsProps = slurpProperties(cliOpsPropertiesFile)
			cliOpsProps.db.url == "jdbc:oracle:thin:@test.apgsga.ch:1521:test"
			cliOpsProps.db.user == "newUser"
			cliOpsProps.db.passwd == "newPassword"
			def cliOpsBackupProps = slurpProperties(cliOpsPropertiesBackupFile)
			cliOpsBackupProps.db.url == "jdbc:oracle:thin:@prod.apgsga.ch:1521:prod"
			cliOpsBackupProps.db.user == "oldUser"
			cliOpsBackupProps.db.passwd == "oldPassword"
			
			// apg-patch-service-server application.properties + backup
			def serverApplicationProps = slurpProperties(serverApplicationPropertiesFile)
			serverApplicationProps.vcs.host == "cvs-t.apgsga.ch"
			serverApplicationProps.vcs.user == "testCvsUser"
			serverApplicationProps.jenkins.host == "https://jenkinsTest.apgsga.ch/"
			serverApplicationProps.jenkins.user == "jenkinsTestUser"
			serverApplicationProps.jenkins.authkey == "jenkinsTestAuthkey"
			def serverApplicationBackupProps = slurpProperties(serverApplicationPropertiesBackupFile)
			serverApplicationBackupProps.vcs.host == "cvs.apgsga.ch"
			serverApplicationBackupProps.vcs.user == "cvsProdUser"
			serverApplicationBackupProps.jenkins.host == "https://jenkins.apgsga.ch/"
			serverApplicationBackupProps.jenkins.user == "jenkinsProdUser"
			serverApplicationBackupProps.jenkins.authkey == "jenkinsProdAuthKey"
	}
	
	private def slurpProperties(def propertyFile) {
		ConfigSlurper cs = new ConfigSlurper()
		
		def props = new Properties()
		propertyFile.withInputStream{ stream ->
			props.load(stream)
		}
		
		def config = cs.parse(props)
		config
	}
	
	private def keepCopyOfOriginalTestFiles() {
		targetSystemMappingOriContent = new JsonSlurper().parse(new File(targetSystemMappingFileName))
		patchCliOriApplicationPropsOriContent = ConfigInitUtil.slurpProperties(new File(patchCliApplicationPropertiesFileName))
		patchCliOpsPropsOriContent = ConfigInitUtil.slurpProperties(new File(patchCliOpsPropertiesFileName))
		patchServerApplicationPropsOriContent = ConfigInitUtil.slurpProperties(new File(patchServerApplicationPropertiesFileName))
		patchServerOpsPropsOriContent = ConfigInitUtil.slurpProperties(new File(patchServerOpsPropertiesFileName))
	}
	
	private def restoreContentOfOriginalTestFiles() {
		def targetSystemMappingFile = new File(targetSystemMappingFileName)
		targetSystemMappingFile.write(new JsonBuilder(targetSystemMappingOriContent).toPrettyString())
		
		Map<ConfigObject,File> configToBeRestored = [:]
		configToBeRestored.put(patchCliOriApplicationPropsOriContent, new File(patchCliApplicationPropertiesFileName))
		configToBeRestored.put(patchCliOpsPropsOriContent, new File(patchCliOpsPropertiesFileName))	
		configToBeRestored.put(patchServerApplicationPropsOriContent, new File(patchServerApplicationPropertiesFileName))
		configToBeRestored.put(patchServerOpsPropsOriContent, new File(patchServerOpsPropertiesFileName))
		
		configToBeRestored.each({configObj,file -> 
			PrintWriter pw = new PrintWriter(file)
			pw.write("")
			
			Properties props = new Properties()
			props.putAll(configObj.flatten())
			
			props.each({key,value ->
				pw.write("${key}=${value}")
				pw.write(System.getProperty("line.separator"))
			})
	
			pw.close()
		})
		
		
	}
	
	private def cleanAllBackupFiles() {
		new File(targetSystemMappingBackupFileName).delete()
		new File(patchCliApplicationPropertiesBackupFileName).delete()
		new File(patchCliOpsPropertiesBackupFileName).delete()
		new File(patchServerApplicationPropertiesBackupFileName).delete()
		new File(patchServerOpsPropertiesBackupFileName).delete()
	}
	
}
