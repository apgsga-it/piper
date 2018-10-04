package com.apgsga.artifact.query.impl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang.exception.ExceptionUtils;
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
import com.apgsga.microservice.patch.api.SearchCondition;
import com.apgsga.microservice.patch.api.impl.MavenArtifactBean;
import com.apgsga.microservice.patch.exceptions.Asserts;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.google.common.collect.Maps;

public class ArtifactManagerImpl implements ArtifactManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactManagerImpl.class);

	private static final String DEFAULT_BOM_GROUP_ID = "com.affichage.common.maven";

	private static final String DEFAULT_BOM_ARTIFACT_ID = "dm-bom";

	private final RepositorySystem system;

	private final RepositorySystemSession session;

	private String bomGroupId = DEFAULT_BOM_GROUP_ID;

	private String bomArtefactId = DEFAULT_BOM_ARTIFACT_ID;

	private final String localRepo;

	private Resource lcoalRepoResource;

	public ArtifactManagerImpl(String localRepo, String bomGroupId, String bomArtefactId) {
		super();
		this.localRepo = localRepo;
		init();
		this.system = RepositorySystemFactory.newRepositorySystem();
		this.session = RepositorySystemFactory.newRepositorySystemSession(system, localRepo);
		this.bomGroupId = bomGroupId;
		this.bomArtefactId = bomArtefactId;

	}

	private void init() {
		final ResourceLoader rl = new FileSystemResourceLoader();
		lcoalRepoResource = rl.getResource(localRepo);
		if (!lcoalRepoResource.exists()) {
			try {
				lcoalRepoResource.getFile().mkdir();
			} catch (IOException e) {
				throw ExceptionFactory.createPatchServiceRuntimeException("ArtifactManagerImpl.init.exception",
						new Object[] { e.getMessage() }, e);
			}
		}
	}

	public ArtifactManagerImpl(String localRepo) {
		super();
		this.system = RepositorySystemFactory.newRepositorySystem();
		this.session = RepositorySystemFactory.newRepositorySystemSession(system, localRepo);
		this.localRepo = localRepo;
	}

	@Override
	public void cleanLocalMavenRepo() {
		LOGGER.info("About to clean Local Mavenrepo");
		final ResourceLoader rl = new FileSystemResourceLoader();
		Resource resource = rl.getResource(localRepo);
		try {
			Path rootPath = Paths.get(resource.getURI());
			Files.walk(rootPath).sorted(Comparator.reverseOrder()).forEach(f -> delete(rootPath, f));
			LOGGER.info("Done cleaning Local Mavenrepo");
		} catch (IOException e) {
			LOGGER.error("File : " + localRepo + " could'nt be deleted", e);
			LOGGER.error(ExceptionUtils.getFullStackTrace(e));
		}

	}

	public static void delete(Path root, Path p) {
		try {
			if (!root.equals(p)) {
				Files.delete(p);
				LOGGER.info("Deleted: " + p.toAbsolutePath());
			}
		} catch (IOException e) {
			LOGGER.error("File : " + p.toFile().getAbsolutePath() + " could'nt be deleted", e);
			LOGGER.error(ExceptionUtils.getFullStackTrace(e));
		}
	}

	@Override
	public File getMavenLocalRepo() {
		try {
			return lcoalRepoResource.getFile();
		} catch (IOException e) {
			LOGGER.error("Directory fetched", e);
			LOGGER.error(ExceptionUtils.getFullStackTrace(e));
			throw ExceptionFactory.createPatchServiceRuntimeException(
					"ArtifactManagerImpl.cleanLocalMavenRepo.exception", new Object[] { e.getMessage() }, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.apgsga.artifact.query.impl.ArtifactManagerI#getVersionsProperties(
	 * java.lang.String)
	 */
	@Override
	public Properties getVersionsProperties(String version)
			throws DependencyResolutionException, IOException, XmlPullParserException, ArtifactResolutionException {
		System.out.println("Version: " + version);
		return getVersionsProperties(loadBomModel(bomGroupId, bomArtefactId, version));
	}

	private Model loadBomModel(String bomGroupId, String bomArtefactId, String version)
			throws ArtifactResolutionException {
		org.eclipse.aether.artifact.Artifact bom = load(bomGroupId, bomArtefactId, version);
		Asserts.notNull(bom, "ArtifactManagerImpl.loadBom.assert", new Object[] { bomGroupId, bomArtefactId, version });
		Model model = null;
		FileReader fileReader = null;
		try {
			MavenXpp3Reader mavenreader = new MavenXpp3Reader();
			File bomFile = bom.getFile();
			fileReader = new FileReader(bomFile);
			model = mavenreader.read(fileReader);
			return model;
		} catch (IOException | XmlPullParserException e) {
			LOGGER.error("Error Loading Bom Model", e);
			LOGGER.error(ExceptionUtils.getFullStackTrace(e));
			throw ExceptionFactory.createPatchServiceRuntimeException(
					"ArtifactManagerImpl.loadBomModel.exception", new Object[] { e.getMessage() }, e);
		} finally {
			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException e) {
					LOGGER.error("Error Closing Filereader from loading Bom Model", e);
					LOGGER.error(ExceptionUtils.getFullStackTrace(e));
				}
			}
		}
	}

	private Artifact load(String groupId, String artifactId, String version) throws ArtifactResolutionException {
		Artifact artifact = new DefaultArtifact(groupId, artifactId, "pom", version);
		ArtifactRequest artifactRequest = new ArtifactRequest();
		artifactRequest.setArtifact(artifact);
		artifactRequest.setRepositories(RepositorySystemFactory.newRepositories());
		try {
			ArtifactResult artifactResult = system.resolveArtifact(session, artifactRequest);
			artifact = artifactResult.getArtifact();
			return artifact;
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if (cause instanceof ArtifactNotFoundException) {
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
	public List<MavenArtifact> getAllDependencies(String serviceVersion)
			throws DependencyResolutionException, ArtifactResolutionException, IOException, XmlPullParserException {
		return getAllDependencies(serviceVersion, SearchCondition.APPLICATION);
	}

	@Override
	public List<MavenArtifact> getAllDependencies(String serviceVersion, SearchCondition searchFilter)
			throws IOException, XmlPullParserException, DependencyResolutionException, ArtifactResolutionException {
		return getArtifactsWithVersionFromBom(serviceVersion, searchFilter);
	}

	private List<MavenArtifact> getArtifactsWithVersionFromBom(String bomVersion, SearchCondition searchFilter)
			throws ArtifactResolutionException {
		Model model = loadBomModel(bomGroupId, bomArtefactId, bomVersion);
		List<MavenArtifact> artifacts = getArtifacts(model);
		List<MavenArtifact> selectedArts = null;
		Properties properties = model.getProperties();
		normalizeVersions(artifacts, properties);
		if (searchFilter.equals(SearchCondition.ALL)) {
			selectedArts = artifacts;
		} else if (searchFilter.equals(SearchCondition.APPLICATION)) {
			selectedArts = artifacts.stream()
					.filter(artifact -> (artifact.getGroupId().startsWith("com.apgsga")
							|| artifact.getGroupId().startsWith("com.affichage"))
							&& artifact.getVersion().endsWith("SNAPSHOT"))
					.collect(Collectors.toList());
		} else {
			selectedArts = Collections.EMPTY_LIST;
		}
		return selectedArts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.apgsga.artifact.query.impl.ArtifactManagerI#
	 * getArtifactsWithNameFromBom(java.lang.String)
	 */
	@Override
	public List<MavenArtifact> getArtifactsWithNameFromBom(String bomVersion)
			throws IOException, XmlPullParserException, DependencyResolutionException, ArtifactResolutionException {
		return getArtifactsWithVersionFromBom(bomVersion, SearchCondition.APPLICATION);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.apgsga.artifact.query.impl.ArtifactManagerI#getArtifactsWithNameAsMap
	 * (java.lang.String)
	 */
	@Override
	public Map<String, String> getArtifactsWithNameAsMap(String version)
			throws DependencyResolutionException, IOException, XmlPullParserException, ArtifactResolutionException {
		List<MavenArtifact> artifacts = getArtifactsWithNameFromBom(version);
		Map<String, String> artMap = Maps.newHashMap();
		for (MavenArtifact art : artifacts) {
			artMap.put(art.getName(), art.getGroupId() + ":" + art.getArtifactId());
		}
		return artMap;
	}

	private static List<MavenArtifact> getArtifacts(Model model) {
		DependencyManagement dependencyManagement = model.getDependencyManagement();
		List<Dependency> dmDeps = dependencyManagement.getDependencies();
		return dmDeps.stream().map(p -> create(p)).collect(Collectors.toList());
	}

	public static MavenArtifact create(Dependency dependency) {
		final MavenArtifactBean art = new MavenArtifactBean();
		art.setArtifactId(dependency.getArtifactId());
		art.setGroupId(dependency.getGroupId());
		art.setVersion(dependency.getVersion());
		return art;
	}

	public static Properties getVersionsProperties(Model model) {
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
			String errMsg = String.format("Artefact : %s  has null Version reference : %s, ignored",
					artifact.toString(), version);
			LOGGER.warn(errMsg);
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
			throws DependencyResolutionException, ArtifactResolutionException, IOException, XmlPullParserException {
		Model model = loadBomModel(groupId, artifactId, version);
		return model.getName();
	}

}
