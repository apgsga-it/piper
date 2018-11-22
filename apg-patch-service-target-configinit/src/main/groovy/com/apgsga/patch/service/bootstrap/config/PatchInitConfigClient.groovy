package com.apgsga.patch.service.bootstrap.config

import groovy.io.FileType
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class PatchInitConfigClient {
	
	ConfigObject initConfig
	
	public PatchInitConfigClient(def initConfig) {
		this.initConfig = initConfig
	}
	
	def initAll() {
		println "init all started ... "

		initTargetSystemMapping()
		initPiperProperties()
		initMavenSettings()
		initGradleSettings()
				
	}

	def initTargetSystemMapping() {
		println "Initialisation of targetSystemMapping started ..."
		backupFile(initConfig.targetSystemMappings)
		changeTargetSystemMappingContent()
		println "Initialisation of targetSystemMapping done!"
	}

	def initPiperProperties() {
		println "Initialisation of patch service properties started ..."
		
		def listFiles = []
		def dir = new File("src/test/resources/etc/opt")
		
		
		dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.properties/) {
			backupFile(it.getPath())
		}
		
		println "Initialisation of patch service properties done!"
		
	}
	
	def initMavenSettings() {
		println "Initialisation of maven settings started ..."
	}
	
	def initGradleSettings() {
		println "Initialisation of graddle settings started ..."
	}
	
	private def changeTargetSystemMappingContent() {
		
		def targetSystemMappingFile = new File(initConfig.targetSystemMappings)
		def targetSystemMappingContent = new JsonSlurper().parse(targetSystemMappingFile)

		updateTargetSystemMapping(targetSystemMappingContent,"Entwicklung")
		updateTargetSystemMapping(targetSystemMappingContent,"Informatiktest")
		updateTargetSystemMapping(targetSystemMappingContent,"Produktion")
		updateTargetSystemMappingOtherInstance(targetSystemMappingContent)
					
		targetSystemMappingFile.delete()
		targetSystemMappingFile.write(new JsonBuilder(targetSystemMappingContent).toPrettyString())
	}
	
	private def updateTargetSystemMappingOtherInstance(def targetSystemMappingContent) {
		targetSystemMappingContent.otherTargetInstances = []
		def newInstancesList = initConfig.target.system.mapping.otherTargetInstances.new
		newInstancesList.split(",").each({instance ->
			targetSystemMappingContent.otherTargetInstances.add(instance)
		})
	}
	
	private def updateTargetSystemMapping(def targetSystemMappingContent, def targetName) {
		targetSystemMappingContent.targetSystems.each({targetSystem ->
			if (targetSystem.name.equals(targetName)) {
				targetSystem.target = getNewTarget(targetName)
			}
		})
	}
	
	private getNewTarget(String targetName) {
		def targetNameLowerCase = targetName.toLowerCase()
		return initConfig.target.system.mapping."${targetNameLowerCase}".new
	}
	
	private def backupFile(def originalFileName) {
		def originalFile = new File(originalFileName)
		def backupFile = new File("${originalFileName}.backup")
		Files.copy(originalFile.toPath(), backupFile.toPath())
		println "Backup created for ${originalFileName} : ${backupFile.getPath()}"
	}

}
