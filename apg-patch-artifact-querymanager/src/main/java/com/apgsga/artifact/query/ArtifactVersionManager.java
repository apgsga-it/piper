package com.apgsga.artifact.query;

import java.net.URI;

import com.apgsga.artifact.query.impl.PropertyFileBasedVersionManager;

public interface ArtifactVersionManager {

	public static ArtifactVersionManager create(URI mavenLocalPath, String bomGroupId,
			String bomArtifactId, String repoUser, String repoUrl) {
		return new PropertyFileBasedVersionManager(mavenLocalPath, bomGroupId, bomArtifactId, repoUser, repoUrl);
	}
	
	public static ArtifactVersionManager create(URI mavenLocalPath, String bomGroupId,
			String bomArtifactId, String patchFilePath, String repoUser, String repoUrl) {
		return new PropertyFileBasedVersionManager(mavenLocalPath, bomGroupId, bomArtifactId, patchFilePath, repoUser, repoUrl);
	}

	public String getVersionFor(String group, String name, String bomVersion);

}
