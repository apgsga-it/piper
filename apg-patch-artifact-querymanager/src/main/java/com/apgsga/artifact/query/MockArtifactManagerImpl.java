package com.apgsga.artifact.query;

import java.io.File;
import java.util.Collections;
import java.util.List;

import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.SearchCondition;

public class MockArtifactManagerImpl implements ArtifactManager {

	@Override
	public List<MavenArtifact> getAllDependencies(String serviceVersion) {
		return Collections.emptyList();
	}

	@Override
	public List<MavenArtifact> getAllDependencies(String serviceVersion, SearchCondition searchFilter) {
		return Collections.emptyList();

	}


	@Override
	public String getArtifactName(String groupId, String artifactId, String version) {
		return artifactId;
	}

	@Override
	public void cleanLocalMavenRepo() {	
		// Nothing
	}

	@Override
	public File getMavenLocalRepo() {
		return null;
	}
	
	

}
