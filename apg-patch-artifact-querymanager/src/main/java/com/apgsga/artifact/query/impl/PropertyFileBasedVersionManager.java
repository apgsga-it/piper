package com.apgsga.artifact.query.impl;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;

import com.apgsga.artifact.query.ArtifactManager;
import com.apgsga.artifact.query.ArtifactVersionManager;

public class PropertyFileBasedVersionManager implements ArtifactVersionManager {

	private ArtifactManager artifactManager;
	private Properties versionsProperties;

	public PropertyFileBasedVersionManager(URI mavenLocalPath, String bomGroupId, String bomArtifactId) {
		super();
		this.artifactManager = ArtifactManager.create(bomGroupId, bomArtifactId, mavenLocalPath.getPath());

	}

	@Override
	public String getVersionFor(String group, String name, String bomVersion) {
		Properties versionsProperties = getProperties(bomVersion);
		return versionsProperties.getProperty(group + ":" + name);

	}

	private Properties getProperties(String bomVersion) {
		Properties versionsProperties = null;
		if (versionsProperties == null) {
			versionsProperties = intialLoad(bomVersion);
		}
		return versionsProperties;
	}

	private Properties intialLoad(String bomVersion) {

		try {
			versionsProperties = artifactManager.getVersionsProperties(bomVersion);
			return versionsProperties;
		} catch (DependencyResolutionException | ArtifactResolutionException | IOException | XmlPullParserException e) {
			throw new RuntimeException(e);
		}
	}

}
