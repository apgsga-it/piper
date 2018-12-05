package com.apgsga.patch.service.client;

import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
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


	// TODO (che,jhe, 18.10) : This is for backword compatability, needs to be
	// elimated
	@Bean
	public ConfigObject configObject() {
		ConfigSlurper sl = new ConfigSlurper();
		Map<String, Object> properties = Maps.newHashMap();
		properties.put("revision.file.path", env.getProperty("revision.file.path"));
		properties.put("host.default", env.getProperty("host.default"));
		properties.put("config.dir", env.getProperty("config.dir"));
		properties.put("onclone.delete.artifact.dryrun", Boolean.getBoolean(env.getProperty("onclone.delete.artifact.dryrun")));
		properties.put("target.system.mapping.file.name", env.getProperty("target.system.mapping.file.name"));
		properties.put("mavenrepo.user.name", env.getProperty("mavenrepo.user.name"));
		properties.put("mavenrepo.baseurl", env.getProperty("mavenrepo.baseurl"));
		properties.put("mavenrepo.name", env.getProperty("mavenrepo.name"));
		properties.put("postclone.list.patch.file.path", env.getProperty("postclone.list.patch.file.path"));
		properties.put("db.url", env.getProperty("db.url",""));
		properties.put("db.user", env.getProperty("db.user",""));
		properties.put("db.passwd", env.getProperty("db.passwd",""));
		properties.put("artifactory.release.repo.name", env.getProperty("artifactory.release.repo.name"));
		properties.put("artifactory.dbpatch.repo.name", env.getProperty("artifactory.dbpatch.repo.name"));
		Properties config = new Properties();
		config.putAll(properties);
		return sl.parse(config);
	}

}
