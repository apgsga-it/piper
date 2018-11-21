package com.apgsga.patch.service.bootstrap.config

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class PatchInitConfigClient {
	
	ConfigObject initConfig
	
	public PatchInitConfigClient(def initConfig) {
		this.initConfig = initConfig
	}
	
	def initAll() {
		println "init all started ... Init config is:"

		initTargetSystemMapping()
		initPatchServiceProperties()
		initMavenSettings()
		initGradleSettings()
				
	}

	def initTargetSystemMapping() {
		println "Initialisation of targetSystemMapping started ..."
		backupFile(initConfig.targetSystemMappings)
	}

	def initPatchServiceProperties() {
		println "Initialisation of patch service properties started ..."
	}
	
	def initMavenSettings() {
		println "Initialisation of maven settings started ..."
	}
	
	def initGradleSettings() {
		println "Initialisation of graddle settings started ..."
	}
	
	private def backupFile(def originalFileName) {
		def originalFile = new File(originalFileName)
		def backupFile = new File("${originalFileName}.backup")
		Files.copy(originalFile.toPath(), backupFile.toPath())
	}

}
