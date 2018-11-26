package com.apgsga.patch.service.bootstrap.config

import groovy.io.FileType
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import com.apgsga.patch.service.configinit.util.ConfigInitUtil

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
		
		// TODO JHE: get dir path from init property file
		def dir = new File("src/test/resources/etc/opt")
		//TODO JHE: get ".properties" from init property file
		dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.properties/) {
			backupFile(it.getPath())
			adaptContentForPiperPropertiesFile(it)
		}
		
		println "Initialisation of patch service properties done!"
		
	}
	
	def adaptContentForPiperPropertiesFile(File file) {
		
		
		println "Process file : " + file.getPath()
		
		def piperPropertiesFromInitConfig = initConfig.flatten()
		Properties propsToBeUpdated = new Properties()
		file.withInputStream{ stream ->
			propsToBeUpdated.load(stream)
		}
		
		piperPropertiesFromInitConfig.forEach({key,value -> 
			println "init key = ${key} , init value = ${value}"
		})

		println "======================================================================================="
		println "======================================================================================="		

		def needToUpdateFile = false
		Properties newProps = new Properties()
		propsToBeUpdated.each({key,value -> 
			
			//println "key = ${key} , value = ${value}"
			
			def String searchedKey = "piper.${key}"
			println "piper key = piper.${key}"
			if(piperPropertiesFromInitConfig.containsKey(searchedKey)) {
				def newValue = piperPropertiesFromInitConfig.get(searchedKey)
				println "${key} will be updated with: ${newValue}"
				newProps.put(key, newValue)
				needToUpdateFile = true
			}
			
		})

		if(needToUpdateFile) {
			propsToBeUpdated.putAll(newProps)
			
			PrintWriter pw = new PrintWriter(file)
			pw.write("")
			
			propsToBeUpdated.each({key,value -> 
				pw.write("${key}=${value}")
				pw.write(System.getProperty("line.separator"))
			})

			pw.close()			
//			propsToBeUpdated.store(file.newWriter(),null)
		}
		
		
		
		println "done"
		
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
