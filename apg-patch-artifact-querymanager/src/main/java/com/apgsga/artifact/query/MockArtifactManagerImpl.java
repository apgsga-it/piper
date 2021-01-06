package com.apgsga.artifact.query;

import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.SearchCondition;

import java.util.Collections;
import java.util.List;

public class MockArtifactManagerImpl implements ArtifactManager {

	@Override
	public List<MavenArtifact> listDependenciesInBom(MavenArtifact bom) {
		return Collections.emptyList();
	}

	@Override
	public List<MavenArtifact> listDependenciesInBom(MavenArtifact bom, SearchCondition searchFilter) {
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
