package com.apgsga.patch.service.client.config

import org.junit.Before
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Configurable
import org.springframework.context.EnvironmentAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.core.env.Environment

@Configuration
@PropertySource(value = "classpath:apscli.properties", ignoreResourceNotFound = false)
class PatchCliConfig {
	
	@Autowired
	def Environment env
	
	public String getRevisionFilePath() {
		def revisionPath = env.getProperty("revision.file.path")
		assert revisionPath != null : "revision.file.path value couldn't be determined, apscli seems to be wrongy configured."
		return revisionPath
	}
}