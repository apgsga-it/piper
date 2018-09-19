package com.apgsga.patch.client.utils

import groovy.json.JsonSlurper

public class TargetSystemMappings {

	private TargetSystemMappings() {
	}

	static def load(config) {
		def mappingFileName = config.target.system.mapping.file.name
		def configDir = config.config.dir
		def targetSystemMappingsFilePath = "${configDir}/${mappingFileName}"
		def targetSystemFile = new File(targetSystemMappingsFilePath)
		def jsonSystemTargets = new JsonSlurper().parseText(targetSystemFile.text)
		def targetSystemMappings = [:]
		jsonSystemTargets.targetSystems.find( { a ->  a.stages.find( { targetSystemMappings.put("${a.name}${it.toState}".toString(),"${it.code}") })} )
		return targetSystemMappings
	}
	
	static def findStatus(config, toStatus) {
		def stateMappings = [:]
		load(config).targetSystems.find( { a ->  a.stages.find( { stateMappings.put("${a.name}${it.toState}".toString(),"${it.code}") })} )
		def statusNum = stateMappings[toStatus]
		if (statusNum == null) {
			println "Error , no Status mapped for ${toStatus}"
			null
		}
		statusNum
	}
}
