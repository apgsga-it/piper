package com.apgsga.patch.service.client.utils

import groovy.json.JsonSlurper
import groovy.transform.Synchronized

@Singleton()
public class TargetSystemMappings {
	
	def targetSystemMappings

	def get() {
		return targetSystemMappings
	}
	
	def findStatus(toStatus) {
		def statusNum = targetSystemMappings[toStatus] 
		if (statusNum == null) {
			println "Error , no Status mapped for ${toStatus}"
			null
		}
		statusNum
	}
	
	def findPredecessorStates(state) {
		def predecessorStates = []
		for (String key : targetSystemMappings.keySet()) {
			def preState = targetSystemMappings[key]
			if (state.toString() == preState) {
				break
			}
			predecessorStates << key
		}
		predecessorStates
	}
	
	@Synchronized
	def load(config) {
		def mappingFileName = config.target.system.mapping.file.name
		def configDir = config.config.dir
		def targetSystemMappingsFilePath = "${configDir}/${mappingFileName}"
		def targetSystemFile = new File(targetSystemMappingsFilePath)
		def jsonSystemTargets = new JsonSlurper().parseText(targetSystemFile.text)
		targetSystemMappings = [:]
		jsonSystemTargets.targetSystems.find( { a ->  a.stages.find( { targetSystemMappings.put("${a.name}${it.toState}".toString(),"${it.code}") })} )
	}
	

}
