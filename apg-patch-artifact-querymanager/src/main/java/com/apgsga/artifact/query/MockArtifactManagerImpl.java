package com.apgsga.artifact.query;

import java.util.Collections;
import java.util.List;

import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.SearchCondition;

public class MockArtifactManagerImpl implements ArtifactManager {

	@Override
	public List<MavenArtifact> getAllDependencies(MavenArtifact bom) {
		return Collections.emptyList();
	}

	@Override
	public List<MavenArtifact> getAllDependencies(MavenArtifact bom, SearchCondition searchFilter) {
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

	
	

}
