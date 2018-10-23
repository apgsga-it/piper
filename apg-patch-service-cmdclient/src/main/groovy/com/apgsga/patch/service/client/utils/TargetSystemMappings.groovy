package com.apgsga.patch.service.client.utils

import groovy.json.JsonSlurper
import groovy.transform.Synchronized

// TODO (che, jhe, 18.10 ): Move Bootstrapping to AppContext
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
	
	def findState(stateCode) {
		for (String key : targetSystemMappings.keySet()) {
			def preState = targetSystemMappings[key]
			if (stateCode.toString() == preState) {
				return key
			}
		}
		null
	}

	
	def findPredecessorStates(state) {
		def predecessorStates = []
		for (String key : targetSystemMappings.keySet()) {
			def preState = targetSystemMappings[key]
			predecessorStates << key
			if (state.toString() == preState) {
				break
			}
		}
		predecessorStates
	}
	
	def relevantStateCode(state,fromToStates) {
		def codeValues = targetSystemMappings.values()
		if (codeValues.contains("${state.toString()}")) {
			return state
		}
		for (def row : fromToStates) {
			def toState = row.TOSTATE
			def fromState = row.FROMSTATE
			if (fromState.equals(state)) {
				return toState
			}
		}
		null
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
