package com.apgsga.patch.service.client.config;

import java.util.Map;
import java.util.Properties;

import com.apgsga.patch.service.client.serverless.PatchServerlessImpl;
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
	@PropertySource({ "${appPropertiesFile}" })
    static class DefaultCliConfig
    { }
	
	@Configuration
    @Profile("dbcli")
	@PropertySource({ "${appPropertiesFile}", "${opsPropertiesFile}" })
    static class DbCliConfig
    { }

	@Configuration
	@Profile("less")
	@ComponentScan({"com.apgsga.microservice.patch.core","com.apgsga.patch.service.client.serverless"})
	@PropertySource({ "${appPropertiesFile}" })
	static class ServerLessConfig
	{
	}



	@Bean
	public ConfigObject configObject() {
		ConfigSlurper sl = new ConfigSlurper();
		Map<String, Object> properties = Maps.newHashMap();
		properties.put("host.default", env.getProperty("host.default",""));
		properties.put("config.dir", env.getProperty("config.dir",""));
		properties.put("target.system.mapping.file.name", env.getProperty("target.system.mapping.file.name",""));
		properties.put("mavenrepo.user.name", env.getProperty("mavenRepoUser",""));
		properties.put("mavenrepo.baseurl", env.getProperty("mavenRepoBaseUrl",""));
		properties.put("mavenrepo.name", env.getProperty("mavenRepoName",""));
		// TODO (che , jhe ) : probably not necessary anymore, but test case dependency
		properties.put("postclone.list.patch.filepath.template", env.getProperty("postclone.list.patch.filepath.template",""));
		properties.put("db.url", env.getProperty("db.url",""));
		properties.put("db.user", env.getProperty("db.user",""));
		properties.put("db.passwd", env.getProperty("db.passwd",""));
		properties.put("json.db.location", env.getProperty("json.db.location",""));
		Properties config = new Properties();
		config.putAll(properties);
		return sl.parse(config);
		
	}

}
