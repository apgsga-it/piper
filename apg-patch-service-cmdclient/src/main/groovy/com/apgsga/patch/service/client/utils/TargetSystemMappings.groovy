package com.apgsga.patch.service.client.utils

import groovy.json.JsonSlurper
import groovy.transform.Synchronized


// TODO (jhe, 6.8.20) : Move this to either Pipeline Code and/or provide a Api
// TODO the later probably being the best. The implementation code do a clone of git a provide use the same groovy code as the Pipelines
// TODO For the moment i am replacing the File with a hardcoded map
@Singleton()
class TargetSystemMappings {

	//TODO JHE : delete this class
	
	def targetSystemMappings = ["EntwicklungInstallationsbereit":2,"InformatiktestInstallationsbereit":15, "AnwendertestInstallationsbereit":25,"ProduktionInstallationsbereit":65]
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

}
