package com.apgsga.artifact.query;

import java.util.List;

import com.apgsga.artifact.query.impl.ArtifactManagerImpl;
import com.apgsga.artifact.query.impl.ArtifactsDependencyResolverImpl;
import com.apgsga.microservice.patch.api.MavenArtifact;

public interface ArtifactDependencyResolver {
	
	public static ArtifactDependencyResolver create(String localRep) {
		return new ArtifactsDependencyResolverImpl(localRep);
	}
	
	void resolveDependencies(List<MavenArtifact> artifacts);

}
