package com.apgsga.artifact.query;

import java.net.URI;

import com.apgsga.artifact.query.impl.PropertyFileBasedVersionManager;

public interface ArtifactVersionManager {

	public static ArtifactVersionManager create(String propertyVersionFilePath, URI mavenLocalPath,
			String bomGroupId, String bomArtifactId, String bomVersionId) {
		return new PropertyFileBasedVersionManager(propertyVersionFilePath, mavenLocalPath, bomGroupId, bomArtifactId);
	}

	public String getVersionFor(String group, String name, String bomVersion);

	public void updateVersion(String group, String name, String version);
}
