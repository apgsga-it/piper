package com.apgsga.artifact.query;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;

import com.apgsga.microservice.patch.api.MavenArtifact;

public class MockArtifactManagerImpl implements ArtifactManager {

	@Override
	public Properties getVersionsProperties(String version)
			throws DependencyResolutionException, IOException, XmlPullParserException, ArtifactResolutionException {
		return null;
	}

	@Override
	public List<MavenArtifact> getAllDependencies(String serviceVersion)
			throws IOException, XmlPullParserException, DependencyResolutionException, ArtifactResolutionException {
		return Collections.emptyList();
	}

	@Override
	public List<MavenArtifact> getArtifactsWithNameFromBom(String bomVersion)
			throws IOException, XmlPullParserException, DependencyResolutionException, ArtifactResolutionException {
		return Collections.emptyList();
	}

	@Override
	public Map<String, String> getArtifactsWithNameAsMap(String version)
			throws DependencyResolutionException, IOException, XmlPullParserException, ArtifactResolutionException {
		return null;
	}

	@Override
	public String getArtifactName(String groupId, String artifactId, String version)
			throws DependencyResolutionException, ArtifactResolutionException, IOException, XmlPullParserException {
		return artifactId;
	}

}
