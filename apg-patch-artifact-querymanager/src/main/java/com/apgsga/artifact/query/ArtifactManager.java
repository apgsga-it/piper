package com.apgsga.artifact.query;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;

import com.apgsga.artifact.query.impl.ArtifactManagerImpl;
import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.SearchCondition;

public interface ArtifactManager {
	

	public static  ArtifactManager create(String groupId, String artefactId, String localRep, RepositorySystemFactory systemFactory) {
		return new ArtifactManagerImpl(localRep, groupId, artefactId, systemFactory);
	}

	public static ArtifactManager create(String localRep, RepositorySystemFactory systemFactory) {
		return new ArtifactManagerImpl(localRep, systemFactory);
	}
	
	public static ArtifactManager createMock(String localRep) {
		return new MockArtifactManagerImpl();
	}


	Properties getVersionsProperties(String version) throws DependencyResolutionException,IOException, XmlPullParserException, ArtifactResolutionException;

	List<MavenArtifact> getAllDependencies(String serviceVersion) throws IOException, XmlPullParserException, DependencyResolutionException,ArtifactResolutionException;

	List<MavenArtifact> getAllDependencies(String serviceVersion, SearchCondition searchFilter) throws IOException, XmlPullParserException, DependencyResolutionException,ArtifactResolutionException;
	
	List<MavenArtifact> getArtifactsWithNameFromBom(String bomVersion) throws IOException, XmlPullParserException, DependencyResolutionException, ArtifactResolutionException, Exception;

	Map<String, String> getArtifactsWithNameAsMap(String version) throws Exception;

	String getArtifactName(String groupId, String artifactId, String version) throws DependencyResolutionException, ArtifactResolutionException, IOException, XmlPullParserException, Exception;
	
	void cleanLocalMavenRepo();
	
	File getMavenLocalRepo();

}