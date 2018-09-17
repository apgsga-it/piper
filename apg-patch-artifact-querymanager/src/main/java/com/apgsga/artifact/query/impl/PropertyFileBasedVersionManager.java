package com.apgsga.artifact.query.impl;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.ResourceLoader;

import com.apgsga.artifact.query.ArtifactManager;
import com.apgsga.artifact.query.ArtifactVersionManager;
import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PropertyFileBasedVersionManager implements ArtifactVersionManager {

	private ArtifactManager artifactManager;
	private Properties versionsProperties;
	private String patchFilePath = "";

	public PropertyFileBasedVersionManager(URI mavenLocalPath, String bomGroupId, String bomArtifactId) {
		super();
		this.artifactManager = ArtifactManager.create(bomGroupId, bomArtifactId, mavenLocalPath.getPath());

	}
	
	public PropertyFileBasedVersionManager(URI mavenLocalPath, String bomGroupId, String bomArtifactId, String patchFilePath) {
		super();
		this.artifactManager = ArtifactManager.create(bomGroupId, bomArtifactId, mavenLocalPath.getPath());
		this.patchFilePath = patchFilePath; 
		
	}

	@Override
	public String getVersionFor(String group, String name, String bomVersion) {
		versionsProperties = getProperties(bomVersion);
		return versionsProperties.getProperty(group + ":" + name);

	}

	private synchronized Properties getProperties(String bomVersion) {
		if (versionsProperties == null) {
			Properties overrideVersionProperties = convertToProperties(patchFilePath); 
			versionsProperties = intialLoad(artifactManager,bomVersion);
			for (Object key : overrideVersionProperties.keySet()) {
				versionsProperties.put(key, overrideVersionProperties.get(key)); 
			}
		}
		return versionsProperties;
	}

	private Properties convertToProperties(String patchFilePath) {
		final Properties properties = new Properties();
		if (StringUtils.isEmpty(patchFilePath)) {
			return properties;
		}
		ResourceLoader rl = new FileSystemResourceLoader();
		ObjectMapper mapper = new ObjectMapper();
		Patch patch;
		try {
			patch = mapper.readValue(rl.getResource(patchFilePath).getFile(), Patch.class);
		} catch (IOException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("PropertyFileBasedVersionManager.convertToProperties.exception",
					new Object[] { patchFilePath, e.getMessage() }, e);
		}
		for (MavenArtifact art : patch.getMavenArtifacts()) {
			if (!art.getVersion().endsWith("SNAPSHOT")) {
				properties.put(art.getGroupId() + ":" + art.getArtifactId(), art.getVersion()); 
			}
		}
		return properties; 
	}

	private static Properties intialLoad(ArtifactManager artifactManager,String bomVersion) {

		try {
			Properties versionsProperties = artifactManager.getVersionsProperties(bomVersion);
			return versionsProperties;
		} catch (DependencyResolutionException | ArtifactResolutionException | IOException | XmlPullParserException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("PropertyFileBasedVersionManager.intialLoad.exception",
					new Object[] { e.getMessage() }, e);
		}
	}
}
