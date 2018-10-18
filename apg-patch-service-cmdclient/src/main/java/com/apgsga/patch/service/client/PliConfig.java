package com.apgsga.patch.service.client;

import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.google.common.collect.Maps;

import groovy.util.ConfigObject;
import groovy.util.ConfigSlurper;

@Configuration
@PropertySource({ "${appPropertiesFile}", "${opsPropertiesFile}" })
public class PliConfig {

	@Value("${revision.file.path}")
	private String revisionFilePath;

	@Value("${revision.range.step}")
	private String revisionRangeStep;

	@Value("${host.default:localhost}")
	private String hostDefault;

	@Value("${config.dir}")
	private String configDir;

	@Value("${onclone.delete.artifact.dryrun}")
	private String oncloneDeleteArtifactDryrun;

	@Value("${target.system.mapping.file.name}")
	private String targetSystemMappingFileName;

	@Value("${artifactory.url")
	private String artifactoryUrl;

	@Value("${artifactory.user}")
	private String artifactoryUser;

	@Value("${artifactory.passwd}")
	private String artifactoryPasswd;

	@Value("${postclone.list.patch.file.path}")
	private String postcloneListPatchFilePath;

	@Value("${db.url}")
	private String dbUrl;

	@Value("${db.user}")
	private String dbUser;

	@Value("${db.passwd}")
	private String dbPasswd;

	// TODO (che,jhe, 18.10) : This is for backword compatability, needs to be
	// elimated
	@Bean
	public ConfigObject configObject() {
		ConfigSlurper sl = new ConfigSlurper();
		Map<String, Object> properties = Maps.newHashMap();
		properties.put("revision.file.path", revisionFilePath);
		properties.put("revision.range.step", revisionRangeStep);
		properties.put("host.default", hostDefault);
		properties.put("config.dir", configDir);
		properties.put("onclone.delete.artifact.dryrun", Boolean.getBoolean(oncloneDeleteArtifactDryrun));
		properties.put("target.system.mapping.file.name", targetSystemMappingFileName);
		properties.put("artifactory.url", artifactoryUrl);
		properties.put("artifactory.passwd", artifactoryPasswd);
		properties.put("postclone.list.patch.file.path", postcloneListPatchFilePath);
		properties.put("db.url", dbUrl);
		properties.put("db.user", dbUser);
		properties.put("db.passwd", dbPasswd);
		Properties config = new Properties();
		config.putAll(properties);
		return sl.parse(config);
	}

}
