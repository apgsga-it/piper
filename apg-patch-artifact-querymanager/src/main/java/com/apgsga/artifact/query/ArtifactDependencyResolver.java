package com.apgsga.artifact.query;

import java.util.List;

import com.apgsga.artifact.query.impl.ArtifactsDependencyResolverImpl;
import com.apgsga.artifact.query.impl.MavenArtWithDependencies;
import com.apgsga.microservice.patch.api.MavenArtifact;

/**
 * @author chhex
 *
 */
public interface ArtifactDependencyResolver {
	
	public static ArtifactDependencyResolver create(String localRep) {
		return new ArtifactsDependencyResolverImpl(localRep);
	}
	
	/**
	 * @param artifacts
	 */
	void resolveDependencies(List<MavenArtifact> artifacts);

}
