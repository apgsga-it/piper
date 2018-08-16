package com.apgsga.artifact.query.impl;

import org.eclipse.aether.AbstractRepositoryListener;
import org.eclipse.aether.RepositoryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jRepositoryListener extends AbstractRepositoryListener {

	protected static final Logger LOGGER = LoggerFactory.getLogger("Aether Query on Local Repo");

	@Override
	public void artifactDescriptorInvalid(RepositoryEvent event) {
		LOGGER.warn("Invalid artifact descriptor for {} : {} ", event.getArtifact(), event.getException().getMessage());
	}

	@Override
	public void artifactDescriptorMissing(RepositoryEvent event) {
		LOGGER.warn("Missing artifact descriptor for {} ", event.getArtifact());
	}

	@Override
	public void artifactResolved(RepositoryEvent event) {
		LOGGER.debug("Resolved artifact: {} from: {} ", event.getArtifact(), event.getRepository());
	}

	@Override
	public void artifactDownloaded(RepositoryEvent event) {
		LOGGER.debug("Downloaded artifact {} from {}", event.getArtifact(), event.getRepository());
	}

	@Override
	public void metadataInvalid(RepositoryEvent event) {
		LOGGER.warn("Invalid metadata {} ", event.getMetadata());
	}

}
