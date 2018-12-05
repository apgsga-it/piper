package com.apgsga.artifact.query;

import java.net.URI;

import com.apgsga.artifact.query.impl.PropertyFileBasedVersionManager;
import com.apgsga.artifact.query.impl.RepositorySystemFactory;

public interface ArtifactVersionManager {

	public static ArtifactVersionManager create(URI mavenLocalPath, String bomGroupId,
			String bomArtifactId, RepositorySystemFactory systemFactory) {
		return new PropertyFileBasedVersionManager(mavenLocalPath, bomGroupId, bomArtifactId, systemFactory);
	}
	
	public static ArtifactVersionManager create(URI mavenLocalPath, String bomGroupId,
			String bomArtifactId, String patchFilePath, RepositorySystemFactory systemFactory) {
		return new PropertyFileBasedVersionManager(mavenLocalPath, bomGroupId, bomArtifactId, patchFilePath, systemFactory);
	}
	
	public String getVersionFor(String group, String name, String bomVersion);

}
