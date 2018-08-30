package com.apgsga.artifact.query.impl;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;

import com.apgsga.artifact.query.ArtifactManager;
import com.apgsga.artifact.query.ArtifactVersionManager;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;

public class PropertyFileBasedVersionManager implements ArtifactVersionManager {

	private ArtifactManager artifactManager;
	private Properties versionsProperties;

	public PropertyFileBasedVersionManager(URI mavenLocalPath, String bomGroupId, String bomArtifactId) {
		super();
		this.artifactManager = ArtifactManager.create(bomGroupId, bomArtifactId, mavenLocalPath.getPath());

	}

	@Override
	public String getVersionFor(String group, String name, String bomVersion) {
		versionsProperties = getProperties(bomVersion);
		return versionsProperties.getProperty(group + ":" + name);

	}

	private synchronized Properties getProperties(String bomVersion) {
		if (versionsProperties == null) {
			versionsProperties = intialLoad(artifactManager,bomVersion);
		}
		return versionsProperties;
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
