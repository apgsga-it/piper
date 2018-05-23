package com.apgsga.artifact.query.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.transfer.ArtifactNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.apgsga.artifact.query.ArtifactManager;
import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.impl.MavenArtifactBean;
import com.google.common.collect.Maps;

public class ArtifactManagerImpl implements ArtifactManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactManagerImpl.class);

	private static final String DEFAULT_BOM_GROUP_ID = "com.affichage.common.maven";

	private static final String DEFAULT_BOM_ARTIFACT_ID = "dm-bom";

	private final RepositorySystem system;

	private final RepositorySystemSession session;

	private String bomGroupId = DEFAULT_BOM_GROUP_ID;

	private String bomArtefactId = DEFAULT_BOM_ARTIFACT_ID;

	public ArtifactManagerImpl(String localRepo, String bomGroupId, String bomArtefactId) {
		super();
		init(localRepo);
		this.system = RepositorySystemFactory.newRepositorySystem();
		this.session = RepositorySystemFactory.newRepositorySystemSession(system, localRepo);
		this.bomGroupId = bomGroupId;
		this.bomArtefactId = bomArtefactId;
	}

	private void init(String localRepo) {
		final ResourceLoader rl = new FileSystemResourceLoader();
		Resource resource = rl.getResource(localRepo);
		if (!resource.exists()) {
			// TODO (che, 25.1) : Do we want this? Correct here?
			try {
				resource.getFile().mkdir();
			} catch (IOException e) {
				throw new RuntimeException("Local Repository directory could'nt be created", e);
			}
		}
	}

	public ArtifactManagerImpl(String localRepo) {
		super();
		this.system = RepositorySystemFactory.newRepositorySystem();
		this.session = RepositorySystemFactory.newRepositorySystemSession(system, localRepo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.apgsga.artifact.query.impl.ArtifactManagerI#getVersionsProperties(
	 * java.lang.String)
	 */
	@Override
	public Properties getVersionsProperties(String version) throws DependencyResolutionException, FileNotFoundException,
			IOException, XmlPullParserException, ArtifactResolutionException {
		org.eclipse.aether.artifact.Artifact bom = load(bomGroupId, bomArtefactId, version);
		if (bom == null)
			throw new RuntimeException("Bom : " + bomGroupId + ", " + bomArtefactId + ", " + version);
		Model model = getModel(bom.getFile());
		return getVersionsProperties(model);
	}

	private Artifact load(String groupId, String artifactId, String version)
			throws DependencyResolutionException, ArtifactResolutionException {
		Artifact artifact = new DefaultArtifact(groupId, artifactId, "pom", version);
		ArtifactRequest artifactRequest = new ArtifactRequest();
		artifactRequest.setArtifact(artifact);
		artifactRequest.setRepositories(RepositorySystemFactory.newRepositories(system, session));
		try {
			ArtifactResult artifactResult = system.resolveArtifact(session, artifactRequest);
			artifact = artifactResult.getArtifact();
			return artifact;
		} catch (Throwable e) {
			Throwable cause = e.getCause();
			if (cause != null && cause instanceof ArtifactNotFoundException) {
				LOGGER.warn("Artifact not found", cause);
				return null;
			}
			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.apgsga.artifact.query.impl.ArtifactManagerI#getArtifacts(java.util.
	 * List, java.lang.String)
	 */
	@Override
	public List<MavenArtifact> getAllDependencies(String serviceVersion) throws FileNotFoundException, DependencyResolutionException, ArtifactResolutionException, IOException, XmlPullParserException {
		return getArtifactsWithNameFromBom(serviceVersion);
	}

	private List<MavenArtifact> getArtifactsWithVersionFromBom(String bomVersion) throws DependencyResolutionException,
			ArtifactResolutionException, FileNotFoundException, IOException, XmlPullParserException {
		org.eclipse.aether.artifact.Artifact bom = null;
		bom = load(bomGroupId, bomArtefactId, bomVersion);
		if (bom == null)
			throw new RuntimeException("Bom : " + bomGroupId + ", " + bomArtefactId + ", " + bomVersion);
		Model model = null;
		model = getModel(bom.getFile());
		List<MavenArtifact> artifacts = getArtifacts(model);
		Properties properties = model.getProperties();
		List<MavenArtifact> selectedArts = artifacts.stream()
				.filter(artifact -> (artifact.getGroupId().startsWith("com.apgsga")
						|| artifact.getGroupId().startsWith("com.affichage"))
				// TODO (che, jhe, 12.12.2017) : what do we want here?
				// && artifact.getVersion().equals(bomVersion)
				).collect(Collectors.toList());
		normalizeVersions(selectedArts, properties);
		return selectedArts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.apgsga.artifact.query.impl.ArtifactManagerI#
	 * getArtifactsWithNameFromBom(java.lang.String)
	 */
	@Override
	public List<MavenArtifact> getArtifactsWithNameFromBom(String bomVersion) throws FileNotFoundException, IOException,
			XmlPullParserException, DependencyResolutionException, ArtifactResolutionException {
		List<MavenArtifact> artifacts = getArtifactsWithVersionFromBom(bomVersion);
		return artifacts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.apgsga.artifact.query.impl.ArtifactManagerI#getArtifactsWithNameAsMap
	 * (java.lang.String)
	 */
	@Override
	public Map<String, String> getArtifactsWithNameAsMap(String version) throws FileNotFoundException,
			DependencyResolutionException, IOException, XmlPullParserException, ArtifactResolutionException {
		List<MavenArtifact> artifacts = getArtifactsWithNameFromBom(version);
		Map<String, String> artMap = Maps.newHashMap();
		for (MavenArtifact art : artifacts) {
			// TODO (che, 14.9) : Bereinigung pom.xml / cvs
			artMap.put(art.getName(), art.getGroupId() + ":" + art.getArtifactId());
		}
		return artMap;
	}

	public static Model getModel(File bomFile) throws FileNotFoundException, IOException, XmlPullParserException {
		MavenXpp3Reader mavenreader = new MavenXpp3Reader();
		Model model = mavenreader.read(new FileReader(bomFile));
		return model;
	}

	private static List<MavenArtifact> getArtifacts(Model model) {
		DependencyManagement dependencyManagement = model.getDependencyManagement();
		List<Dependency> dmDeps = dependencyManagement.getDependencies();
		List<MavenArtifact> artifacts = dmDeps.stream().map(p -> create(p)).collect(Collectors.toList());
		return artifacts;
	}

	public static MavenArtifact create(Dependency dependency) {
		final MavenArtifactBean art = new MavenArtifactBean();
		art.setArtifactId(dependency.getArtifactId());
		art.setGroupId(dependency.getGroupId());
		art.setVersion(dependency.getVersion());
		return art;
	}

	public static Properties getVersionsProperties(Model model)
			throws FileNotFoundException, IOException, XmlPullParserException {
		final List<MavenArtifact> artifacts = getArtifacts(model);
		Properties properties = model.getProperties();
		normalizeVersions(artifacts, properties);
		return toProperties(artifacts);
	}

	private static Properties toProperties(List<MavenArtifact> artifacts) {
		Properties properties = new Properties();
		artifacts.stream()
				.forEach(art -> properties.put(art.getGroupId() + ":" + art.getArtifactId(), art.getVersion()));
		return properties;
	}

	private static void normalizeVersions(List<MavenArtifact> artifacts, Properties properties) {
		for (MavenArtifact artifact : artifacts) {
			String version = artifact.getVersion();
			if (version.startsWith("${") && version.endsWith("}")) {
				normalizeVersion(artifact, version, properties);
			}
		}
	}

	private static void normalizeVersion(MavenArtifact artifact, String version, Properties properties) {
		String key = version.substring(2, version.length() - 1);
		String value = properties.getProperty(key);
		if (value == null) {
			LOGGER.warn("Artefact : " + artifact.toString() + " has null Version reference : " + version + ", ignored");
			return;
		}
		if (value.startsWith("${") && value.endsWith("}")) {
			normalizeVersion(artifact, value, properties);
			return;
		}
		artifact.setVersion(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.apgsga.artifact.query.impl.ArtifactManagerI#getArtifactName(java.lang
	 * .String, java.lang.String, java.lang.String)
	 */
	@Override
	public String getArtifactName(String groupId, String artifactId, String version)
			throws DependencyResolutionException, ArtifactResolutionException, FileNotFoundException, IOException,
			XmlPullParserException {
		org.eclipse.aether.artifact.Artifact pom = load(groupId, artifactId, version);
		if (pom != null) {
			Model model = ArtifactManagerImpl.getModel(pom.getFile());
			return model.getName();
		}
		return null;
	}

}
