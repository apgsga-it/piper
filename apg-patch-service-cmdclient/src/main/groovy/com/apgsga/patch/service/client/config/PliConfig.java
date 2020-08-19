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
		// TODO (che , jhe ) : probably not necessary anymore, but test case dependency
		properties.put("postclone.list.patch.filepath.template", env.getProperty("postclone.list.patch.filepath.template",""));
		Properties config = new Properties();
		config.putAll(properties);
		return sl.parse(config);
		
	}
}
