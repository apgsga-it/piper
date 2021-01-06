package com.apgsga.artifact.query;

import com.apgsga.microservice.patch.api.MavenArtifact;

import java.util.List;

public class MockDependencyResolverImpl implements ArtifactDependencyResolver {

	@Override
	public void resolveDependencies(List<MavenArtifact> artifacts) {
	}

}
