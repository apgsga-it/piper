package com.apgsga.artifact.query.impl;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import com.apgsga.artifact.query.ArtifactManager;
import com.apgsga.artifact.query.ArtifactVersionManager;
import com.apgsga.artifact.query.RepositorySystemFactory;
import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.Patch;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PropertyFileBasedVersionManager implements ArtifactVersionManager {

	protected static final Logger LOGGER = LoggerFactory.getLogger(ArtifactsDependencyResolverImpl.class);

	private ArtifactManager artifactManager;
	private Properties versionsProperties;
	private String patchFilePath = "";
	private String lastBomVersion = "";

	public PropertyFileBasedVersionManager(URI mavenLocalPath, String bomGroupId, String bomArtifactId, RepositorySystemFactory systemFactory) {
		super();
		this.artifactManager = ArtifactManager.create(bomGroupId, bomArtifactId, mavenLocalPath.getPath(), systemFactory);

	}

	public PropertyFileBasedVersionManager(URI mavenLocalPath, String bomGroupId, String bomArtifactId,
			String patchFilePath, RepositorySystemFactory systemFactory) {
		super();
		this.artifactManager = ArtifactManager.create(bomGroupId, bomArtifactId, mavenLocalPath.getPath(), systemFactory);
		this.patchFilePath = patchFilePath;

	}

	@Override
	public String getVersionFor(String group, String name, String bomVersion) {
		versionsProperties = getProperties(bomVersion);
		String version = versionsProperties.getProperty(group + ":" + name);
		return version;

	}

	private synchronized Properties getProperties(String bomVersion) {
		if (versionsProperties == null || !bomVersion.equals(lastBomVersion)) {
			versionsProperties = intialLoad(artifactManager, bomVersion);
			if (!StringUtils.isEmpty(patchFilePath)) {
				Patch patch = loadPatchFile(patchFilePath);
				mergeProperties(patch, versionsProperties);
			}
			lastBomVersion = bomVersion;
		}
		return versionsProperties;
	}

	private Patch loadPatchFile(String patchFilePath) {
		ResourceLoader rl = new FileSystemResourceLoader();
		ObjectMapper mapper = new ObjectMapper();
		Patch patch;
		try {
			patch = mapper.readValue(rl.getResource(patchFilePath).getFile(), Patch.class);
		} catch (IOException e) {
			LOGGER.error(ExceptionUtils.getFullStackTrace(e));
			throw new PatchFileAccessException(e);
		}
		return patch;
	}

	private void mergeProperties(Patch patch, Properties currentProperties) {
		for (MavenArtifact art : patch.getMavenArtifacts()) {
			String key = art.getGroupId() + ":" + art.getArtifactId();
			// If Patch File contains a "Library" or a new Artifact , it needs to get merged
			if (!art.getVersion().endsWith("SNAPSHOT") || !currentProperties.containsKey(key)) {
				currentProperties.put(key, art.getVersion());
			}
		}
	}

	private static Properties intialLoad(ArtifactManager artifactManager, String bomVersion) {

		try {
			Properties versionsProperties = artifactManager.getVersionsProperties(bomVersion);
			return versionsProperties;
		} catch (DependencyResolutionException | ArtifactResolutionException | IOException | XmlPullParserException e) {
			LOGGER.error(ExceptionUtils.getFullStackTrace(e));
			throw new PatchFileAccessException(e);
		}
	}
}
