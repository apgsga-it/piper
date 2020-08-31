package com.apgsga.microservice.patch.core.impl;

import com.apgsga.microservice.patch.api.Service;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.apgsga.artifact.query.ArtifactDependencyResolver;
import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.core.impl.jenkins.JenkinsClient;
import com.apgsga.microservice.patch.core.commands.patch.vcs.PatchSshCommand;
import com.apgsga.microservice.patch.core.commands.CommandRunner;

public class TaskEntwicklungInstallationsbereit implements Runnable {

	protected static final Log LOGGER = LogFactory.getLog(TaskEntwicklungInstallationsbereit.class.getName());

	public static Runnable create(CommandRunner jschSession, Patch patch, ArtifactDependencyResolver dependencyResolver,
								  JenkinsClient jenkinsPatchClient, PatchPersistence repo) {
		return new TaskEntwicklungInstallationsbereit(jschSession, patch, dependencyResolver, jenkinsPatchClient, repo);
	}

	private final CommandRunner jschSession;
	private final Patch patch;
	private final ArtifactDependencyResolver dependencyResolver;
	private final JenkinsClient jenkinsPatchClient;
	private final PatchPersistence repo;

	private TaskEntwicklungInstallationsbereit(CommandRunner jschSession, Patch patch,
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
			jschSession.run(PatchSshCommand.createTagPatchModulesCmd(patch.getPatchTag(), patch.getDbPatchBranch(),
					patch.getDbObjectsAsVcsPath()));
		}
		// TODO (MULTISERVICE_CM , 9.4) : Needs to be verified
		for (Service service : patch.getServices() ) {
			if (!service.getMavenArtifactsAsVcsPath().isEmpty()) {
				jschSession.run(PatchSshCommand.createTagPatchModulesCmd(patch.getPatchTag(), service.getMicroServiceBranch(),
						service.getMavenArtifactsAsVcsPath()));
			}
		}
		jschSession.postProcess();
		for (Service service : patch.getServices() ) {
			dependencyResolver.resolveDependencies(service.getMavenArtifacts());
		}

		repo.savePatch(patch);
		LOGGER.info("Running EntwicklungInstallationsbereitAction startProdPatchPipeline");
		jenkinsPatchClient.startProdPatchPipeline(patch);
	}

}
