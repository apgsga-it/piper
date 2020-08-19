package com.apgsga.patch.service.client.config;

import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

import com.google.common.collect.Maps;

import groovy.util.ConfigObject;
import groovy.util.ConfigSlurper;

@Configuration
public class PliConfig {

	@Autowired
	private Environment env;
	
	@Configuration
    @Profile("default")
	@PropertySource({ "${appPropertiesFile}"})
    static class DefaultCliConfig
    { }
	
	@Bean
	public ConfigObject configObject() {
		ConfigSlurper sl = new ConfigSlurper();
		Map<String, Object> properties = Maps.newHashMap();
		properties.put("host.default", env.getProperty("host.default",""));
		Properties config = new Properties();
		config.putAll(properties);
		return sl.parse(config);
		
	}
}
