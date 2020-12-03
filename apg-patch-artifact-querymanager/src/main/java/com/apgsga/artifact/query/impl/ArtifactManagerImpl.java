package com.apgsga.artifact.query.impl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
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
import org.eclipse.aether.transfer.ArtifactNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.apgsga.artifact.query.ArtifactManager;
import com.apgsga.artifact.query.RepositorySystemFactory;
import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.SearchCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

public class ArtifactManagerImpl implements ArtifactManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactManagerImpl.class);

	private static final String DEFAULT_BOM_GROUP_ID = "com.affichage.common.maven";

	private static final String DEFAULT_BOM_ARTIFACT_ID = "dm-bom";

	private static final Map<SearchCondition, String> templateMap = Maps.newConcurrentMap();

	static {
		templateMap.put(SearchCondition.FORMS2JAVA, "classpath:templateForms2Java.json");
		templateMap.put(SearchCondition.PERSISTENT, "classpath:templatePersistence.json");
		templateMap.put(SearchCondition.IT21UI, "classpath:templateIt21Ui.json");
	}

	private final RepositorySystemFactory systemFactory;
	
	private final RepositorySystem system;

	private final RepositorySystemSession session;

	private String bomGroupId = DEFAULT_BOM_GROUP_ID;

	private String bomArtefactId = DEFAULT_BOM_ARTIFACT_ID;

	private final String localRepo;

	private Resource localRepoResource;

	public ArtifactManagerImpl(String localRepo, String bomGroupId, String bomArtefactId, RepositorySystemFactory systemFactory) {
		super();
		this.localRepo = localRepo;
		init();
		this.systemFactory = systemFactory;
		this.system = systemFactory.newRepositorySystem();
		this.session = systemFactory.newRepositorySystemSession(system, localRepo);
		this.bomGroupId = bomGroupId;
		this.bomArtefactId = bomArtefactId;

	}

	private void init() {
		final ResourceLoader rl = new FileSystemResourceLoader();
		localRepoResource = rl.getResource(localRepo);
		if (!localRepoResource.exists()) {
			try {
				localRepoResource.getFile().mkdir();
			} catch (IOException e) {
				throw new RuntimeException("Could not create Local Maven Repo upon initialization",e);
			}
		}
	}

	public ArtifactManagerImpl(String localRepo, RepositorySystemFactory systemFactory) {
		super();
		this.systemFactory = systemFactory;
		this.system = systemFactory.newRepositorySystem();
		this.session = systemFactory.newRepositorySystemSession(system, localRepo);
		this.localRepo = localRepo;
	}

	@Override
	public void cleanLocalMavenRepo() {
		LOGGER.info("About to clean Local Maven Repo");
		cleanLocalMavenRepo("com/affichage");
		cleanLocalMavenRepo("com/apgsga"); 
	}
	
	private void cleanLocalMavenRepo(String path) {
		LOGGER.info("About to clean Local Maven Repo");
		final ResourceLoader rl = new FileSystemResourceLoader();
		Resource resource = rl.getResource(localRepo + "/" + path);
		try {
			Path rootPath = Paths.get(resource.getURI());
			Files.walk(rootPath).sorted(Comparator.reverseOrder()).forEach(f -> delete(rootPath, f));
			LOGGER.info("Done cleaning Local Maven Repo");
		} catch (IOException e) {
			LOGGER.error("File : " + localRepo + " couldn't be deleted", e);
			LOGGER.error(ExceptionUtils.getFullStackTrace(e));
		}
	}

	public static void delete(Path root, Path p) {
		try {
			if (!root.equals(p)) {
				Files.delete(p);
				LOGGER.info("Deleted: {} ", p.toAbsolutePath());
			}
		} catch (IOException e) {
			LOGGER.error("File : " + p.toFile().getAbsolutePath() + " couldn't be deleted", e);
			LOGGER.error(ExceptionUtils.getFullStackTrace(e));
		}
	}

	@Override
	public File getMavenLocalRepo() {
		try {
			return localRepoResource.getFile();
		} catch (IOException e) {
			LOGGER.error("Directory fetched", e);
			LOGGER.error(ExceptionUtils.getFullStackTrace(e));
			throw new RuntimeException("Exception while retrieving Local Maven Repo", e);
		}
	}


	private Model loadBomModel(String bomGroupId, String bomArtefactId, String version)
			throws ArtifactResolutionException {
		org.eclipse.aether.artifact.Artifact bom = load(bomGroupId, bomArtefactId, version);
		Preconditions.checkNotNull(bom,String.format("Bom with artifactId: %s, groupId: %s and version %s couldn't be loaded",bomArtefactId,bomGroupId,version));
		FileReader fileReader = null;
		try {
			MavenXpp3Reader mavenReader = new MavenXpp3Reader();
			File bomFile = bom.getFile();
			fileReader = new FileReader(bomFile);
			return mavenReader.read(fileReader);
		} catch (IOException | XmlPullParserException e) {
			LOGGER.error("Error Loading Bom Model", e);
			LOGGER.error(ExceptionUtils.getFullStackTrace(e));
			throw new RuntimeException(String.format("Bom with artifactId: %s, groupId: %s and version %s couldn't be parsed",bomArtefactId,bomGroupId,version),e);
		} finally {
			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException e) {
					LOGGER.error("Error Closing File Reader from loading Bom Model", e);
					LOGGER.error(ExceptionUtils.getFullStackTrace(e));
				}
			}
		}
	}

	private Artifact load(String groupId, String artifactId, String version) throws ArtifactResolutionException {
		Artifact artifact = new DefaultArtifact(groupId, artifactId, "pom", version);
		ArtifactRequest artifactRequest = new ArtifactRequest();
		artifactRequest.setArtifact(artifact);
		artifactRequest.setRepositories(this.systemFactory.newRepositories());
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
			throws ArtifactResolutionException {
		return getAllDependencies(serviceVersion, SearchCondition.APPLICATION);
	}

	@Override
	public List<MavenArtifact> getAllDependencies(String serviceVersion, SearchCondition searchFilter)
			throws ArtifactResolutionException {
		return getArtifactsWithVersionFromBom(serviceVersion, searchFilter);
	}

	private List<MavenArtifact> getArtifactsWithVersionFromBom(String bomVersion, SearchCondition searchFilter)
			throws ArtifactResolutionException {
		Model model = loadBomModel(bomGroupId, bomArtefactId, bomVersion);
		final List<MavenArtifact> read = getArtifacts(model);
		Properties properties = model.getProperties();
		final List<MavenArtifact> artifacts = normalizeVersions(read, properties);
		if (searchFilter.equals(SearchCondition.ALL)) {
			return artifacts;
		} else if (searchFilter.equals(SearchCondition.APPLICATION)) {
			return artifacts.stream()
					.filter(artifact -> (artifact.getGroupId().startsWith("com.apgsga")
							|| artifact.getGroupId().startsWith("com.affichage"))
							&& artifact.getVersion().endsWith("SNAPSHOT"))
					.collect(Collectors.toList());
		} else {
			if (templateMap.containsKey(searchFilter)) {
				ResourceLoader rl = new FileSystemResourceLoader();
				Resource resource = rl.getResource(templateMap.get(searchFilter));
				ObjectMapper mapper = new ObjectMapper();
				try {
					MavenArtifact[] template = mapper.readValue(resource.getInputStream(), MavenArtifact[].class);
					List<MavenArtifact> templateList = Arrays.asList(template);
					return artifacts.stream().filter(templateList::contains).collect(Collectors.toList());
				} catch (IOException e) {
					throw new RuntimeException("ArtifactManagerImpl.getArtifactsWithVersionFromBom.exception", e);
				}
			}
			return Collections.emptyList();
		}
	}


	private static List<MavenArtifact> getArtifacts(Model model) {
		DependencyManagement dependencyManagement = model.getDependencyManagement();
		List<Dependency> dmDeps = dependencyManagement.getDependencies();
		return dmDeps.stream().map(ArtifactManagerImpl::create).collect(Collectors.toList());
	}

	public static MavenArtifact create(Dependency dependency) {
		return  MavenArtifact.builder()
				.artifactId(dependency.getArtifactId())
				.groupId(dependency.getGroupId())
				.version(dependency.getVersion()).build();
	}

	private static List<MavenArtifact> normalizeVersions(List<MavenArtifact> artifacts, Properties properties) {
		List<MavenArtifact> mavenArtifacts = Lists.newArrayList();
		for (MavenArtifact artifact : artifacts) {
			String version = artifact.getVersion();
			if (version.startsWith("${") && version.endsWith("}")) {
				mavenArtifacts.add(normalizeVersion(artifact, version, properties));
			}
			mavenArtifacts.add(artifact);
		}
		return mavenArtifacts;
	}

	private static MavenArtifact normalizeVersion(MavenArtifact artifact, String version, Properties properties) {
		String key = version.substring(2, version.length() - 1);
		String value = properties.getProperty(key);
		if (value == null) {
			String errMsg = String.format("Artefact : %s  has null Version reference : %s, ignored",
					artifact.toString(), version);
			LOGGER.warn(errMsg);
			return artifact;
		}
		if (value.startsWith("${") && value.endsWith("}")) {
			normalizeVersion(artifact, value, properties);
			return artifact;
		}
		return MavenArtifact.builder()
				.artifactId(artifact.getArtifactId())
				.groupId(artifact.getGroupId())
				.version(value).build();
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
			throws Exception {
		Model model = loadBomModel(groupId, artifactId, version);
		return model.getName();
	}

}
