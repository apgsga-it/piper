package com.apgsga.artifact.query;

import com.apgsga.artifact.query.impl.ArtifactManagerImpl;
import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.SearchCondition;

import java.util.List;

public interface ArtifactManager {


	static ArtifactManager create(String localRep, RepositorySystemFactory systemFactory) {
		return new ArtifactManagerImpl(localRep, systemFactory);
	}

	@SuppressWarnings("unused")
    static ArtifactManager createMock(String localRep) {
		return new MockArtifactManagerImpl();
	}

	List<MavenArtifact> getAllDependencies(MavenArtifact bom);

	List<MavenArtifact> getAllDependencies(MavenArtifact bom, SearchCondition searchFilter);

	String getArtifactName(String groupId, String artifactId, String version);
	
	void cleanLocalMavenRepo();


}