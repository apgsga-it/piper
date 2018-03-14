package com.apgsga.artifact.query;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;

import com.apgsga.artifact.query.impl.ArtifactManagerImpl;
import com.apgsga.microservice.patch.api.MavenArtifact;

public interface ArtifactManager {
	

	public static  ArtifactManager create(String groupId, String artefactId, String localRep) {
		return new ArtifactManagerImpl(localRep, groupId, artefactId);
	}

	public static ArtifactManager create(String localRep) {
		return new ArtifactManagerImpl(localRep);
	}

	Properties getVersionsProperties(String version) throws DependencyResolutionException, FileNotFoundException,IOException, XmlPullParserException, ArtifactResolutionException;

	List<MavenArtifact> getAllDependencies(String serviceVersion) throws FileNotFoundException, IOException, XmlPullParserException, DependencyResolutionException,ArtifactResolutionException;

	List<MavenArtifact> getArtifactsWithNameFromBom(String bomVersion) throws FileNotFoundException, IOException, XmlPullParserException, DependencyResolutionException, ArtifactResolutionException;

	Map<String, String> getArtifactsWithNameAsMap(String version) throws FileNotFoundException, DependencyResolutionException, IOException, XmlPullParserException, ArtifactResolutionException;

	String getArtifactName(String groupId, String artifactId, String version) throws DependencyResolutionException, ArtifactResolutionException, FileNotFoundException, IOException, XmlPullParserException;

}