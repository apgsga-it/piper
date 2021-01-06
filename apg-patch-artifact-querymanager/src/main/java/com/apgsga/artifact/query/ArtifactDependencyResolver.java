package com.apgsga.artifact.query;

import com.apgsga.artifact.query.impl.ArtifactsDependencyResolverImpl;
import com.apgsga.microservice.patch.api.MavenArtifact;

import java.util.List;

/**
 * @author chhex
 *
 */
public interface ArtifactDependencyResolver {
	
	static ArtifactDependencyResolver create(String localRep, RepositorySystemFactory systemFactory) {
		return new ArtifactsDependencyResolverImpl(localRep,systemFactory);
	}
	
	static ArtifactDependencyResolver createMock(String localRep) {
		return new MockDependencyResolverImpl();
	}
	
	/**
	 * @param artifacts List of Artifacts to resolve their dependencies
	 */
	void resolveDependencies(List<MavenArtifact> artifacts);

}
