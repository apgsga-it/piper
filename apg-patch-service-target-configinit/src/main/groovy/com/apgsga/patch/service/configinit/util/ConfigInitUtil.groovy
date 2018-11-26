package com.apgsga.patch.service.configinit.util

class ConfigInitUtil {
	
	public static ConfigObject slurpProperties(def propertyFile) {
		ConfigSlurper cs = new ConfigSlurper()
		
		def props = new Properties()
		propertyFile.withInputStream{ stream ->
			props.load(stream)
		}
		
		cs.parse(props)
	}

}
