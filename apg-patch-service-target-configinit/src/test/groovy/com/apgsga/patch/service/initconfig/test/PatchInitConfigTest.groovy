package com.apgsga.patch.service.initconfig.test

import java.nio.file.Files
import java.nio.file.Paths

import javax.imageio.ImageIO.ContainsFilter

import org.spockframework.util.Assert

import com.apgsga.patch.service.bootstrap.config.PatchInitConfigCli

import groovy.json.JsonSlurper
import spock.lang.Specification

class PatchInitConfigTest extends Specification {
	
	def usageString = "usage: patchinitcli.sh -[h|i|]"	
	
	def targetSystemMappingBackupFileName = "src/test/resources/etc/opt/apg-patch-common/TargetSystemMappings.json.backup"
	
	def targetSystemMappingFileName = "src/test/resources/etc/opt/apg-patch-common/TargetSystemMappings.json"
	
	def setup() {
		println "TODO Setup if required ..."
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
	
	def "PatchInitConfig validate init for TargetSystemMapping File"() {
		when:
			PatchInitConfigCli cli = PatchInitConfigCli.create()
			def targetSystemMappingFile = new File(targetSystemMappingFileName)
			def result = cli.process(["-i","src/test/resources/initconfig.properties"])
		then:
			result.returnCode == 0
			new File(targetSystemMappingBackupFileName).exists()
			targetSystemMappingFile.exists()
			
			// Validate content of backup file
			def targetSystemMappingBackupFileContent = new JsonSlurper().parse(targetSystemMappingFile)
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
						Assert.that(targetSystem.target.equals("CHEI212"), "New target for Produktion should be CHPI211!")
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
			
		cleanup:
			new File(targetSystemMappingBackupFileName).delete()
	}
	
}
