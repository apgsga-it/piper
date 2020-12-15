package com.apgsga.artifact.query;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;

import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.SearchCondition;

public class MockArtifactManagerImpl implements ArtifactManager {

	@Override
	public List<MavenArtifact> getAllDependencies(String serviceVersion)
			throws IOException, XmlPullParserException, DependencyResolutionException, ArtifactResolutionException {
		return Collections.emptyList();
	}

	@Override
	public List<MavenArtifact> getAllDependencies(String serviceVersion, SearchCondition searchFilter)
			throws IOException, XmlPullParserException, DependencyResolutionException, ArtifactResolutionException {
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
