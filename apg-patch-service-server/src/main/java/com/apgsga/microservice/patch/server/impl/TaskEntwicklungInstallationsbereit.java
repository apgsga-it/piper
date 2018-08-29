package com.apgsga.microservice.patch.server.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.apgsga.artifact.query.ArtifactDependencyResolver;
import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.server.impl.jenkins.JenkinsClient;
import com.apgsga.microservice.patch.server.impl.vcs.PatchVcsCommand;
import com.apgsga.microservice.patch.server.impl.vcs.VcsCommandRunner;

public class TaskEntwicklungInstallationsbereit implements Runnable {

	protected static final Log LOGGER = LogFactory.getLog(TaskEntwicklungInstallationsbereit.class.getName());

	public static Runnable create(VcsCommandRunner jschSession, Patch patch, ArtifactDependencyResolver dependencyResolver,
			JenkinsClient jenkinsPatchClient, PatchPersistence repo) {
		return new TaskEntwicklungInstallationsbereit(jschSession, patch, dependencyResolver, jenkinsPatchClient, repo);
	}

	private final VcsCommandRunner jschSession;
	private final Patch patch;
	private final ArtifactDependencyResolver dependencyResolver;
	private final JenkinsClient jenkinsPatchClient;
	private final PatchPersistence repo;

	private TaskEntwicklungInstallationsbereit(VcsCommandRunner jschSession, Patch patch,
			ArtifactDependencyResolver dependencyResolver, JenkinsClient jenkinsPatchClient, PatchPersistence repo) {
		super();
		this.jschSession = jschSession;
		this.patch = patch;
		this.dependencyResolver = dependencyResolver;
		this.jenkinsPatchClient = jenkinsPatchClient;
		this.repo = repo;
	}

	@Override
	public void run() {
		LOGGER.info("Running EntwicklungInstallationsbereitAction PatchVcsCommands");
		jschSession.preProcess();
		if (!patch.getDbObjectsAsVcsPath().isEmpty()) {
			jschSession.run(PatchVcsCommand.createTagPatchModulesCmd(patch.getPatchTag(), patch.getDbPatchBranch(),
					patch.getDbObjectsAsVcsPath()));
		}
		if (!patch.getMavenArtifactsAsVcsPath().isEmpty()) {
			jschSession.run(PatchVcsCommand.createTagPatchModulesCmd(patch.getPatchTag(), patch.getMicroServiceBranch(),
					patch.getMavenArtifactsAsVcsPath()));
		}
		jschSession.postProcess();
		dependencyResolver.resolveDependencies(patch.getMavenArtifacts());
		repo.savePatch(patch);
		LOGGER.info("Running EntwicklungInstallationsbereitAction startProdPatchPipeline");
		jenkinsPatchClient.startProdPatchPipeline(patch);
	}

}
