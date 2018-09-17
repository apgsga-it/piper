package com.apgsga.artifact.query.impl;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;

import com.apgsga.artifact.query.ArtifactManager;
import com.apgsga.artifact.query.ArtifactVersionManager;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.google.common.collect.Lists;

public class PropertyFileBasedVersionManager implements ArtifactVersionManager {

	private ArtifactManager artifactManager;
	private Properties versionsProperties;
	private List<Map<String,String>> mavenArtefactsToOverride = Lists.newArrayList();

	public PropertyFileBasedVersionManager(URI mavenLocalPath, String bomGroupId, String bomArtifactId) {
		super();
		this.artifactManager = ArtifactManager.create(bomGroupId, bomArtifactId, mavenLocalPath.getPath());

	}
	
	public PropertyFileBasedVersionManager(URI mavenLocalPath, String bomGroupId, String bomArtifactId, List<Map<String,String>>  mavenArtefactsToOverride) {
		super();
		this.artifactManager = ArtifactManager.create(bomGroupId, bomArtifactId, mavenLocalPath.getPath());
		this.mavenArtefactsToOverride = mavenArtefactsToOverride;
	}

	@Override
	public String getVersionFor(String group, String name, String bomVersion) {
		versionsProperties = getProperties(bomVersion);
		return versionsProperties.getProperty(group + ":" + name);

	}

	private synchronized Properties getProperties(String bomVersion) {
		if (versionsProperties == null) {
			Properties overrideVersionProperties = convertToProperties(mavenArtefactsToOverride); 
			versionsProperties = intialLoad(artifactManager,bomVersion);
			for (Object key : overrideVersionProperties.keySet()) {
				versionsProperties.put(key, overrideVersionProperties.get(key)); 
			}
		}
		return versionsProperties;
	}

	@SuppressWarnings("unchecked")
	private Properties convertToProperties(List<Map<String,String>> mavenArtefactsToOverride) {
		final Properties properties = new Properties();
		for (Map<String,String> art : mavenArtefactsToOverride) {
			if (!art.get("version").endsWith("SNAPSHOT")) {
				properties.put(art.get("groupId") + ":" + art.get("artifactId"), art.get("version")); 
			}
		}
		return properties; 
	}

	private static Properties intialLoad(ArtifactManager artifactManager,String bomVersion) {

		try {
			Properties versionsProperties = artifactManager.getVersionsProperties(bomVersion);
			return versionsProperties;
		} catch (DependencyResolutionException | ArtifactResolutionException | IOException | XmlPullParserException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("ArtifactsDependencyResolverImpl.init.exception",
					new Object[] { e.getMessage() }, e);
		}
	}
}
