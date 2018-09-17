package com.apgsga.artifact.query;

import java.net.URI;

import com.apgsga.artifact.query.impl.PropertyFileBasedVersionManager;

public interface ArtifactVersionManager {

	public static ArtifactVersionManager create(URI mavenLocalPath, String bomGroupId,
			String bomArtifactId) {
		return new PropertyFileBasedVersionManager(mavenLocalPath, bomGroupId, bomArtifactId);
	}
	
	public static ArtifactVersionManager create(URI mavenLocalPath, String bomGroupId,
			String bomArtifactId,  String patchFilePath) {
		return new PropertyFileBasedVersionManager(mavenLocalPath, bomGroupId, bomArtifactId, patchFilePath);
	}

	public String getVersionFor(String group, String name, String bomVersion);

}
