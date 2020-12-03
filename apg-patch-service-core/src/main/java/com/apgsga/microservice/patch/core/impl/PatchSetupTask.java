package com.apgsga.microservice.patch.core.impl;

import com.apgsga.artifact.query.ArtifactDependencyResolver;
import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.api.Service;
import com.apgsga.microservice.patch.api.SetupParameter;
import com.apgsga.microservice.patch.core.commands.CommandRunner;
import com.apgsga.microservice.patch.core.commands.patch.vcs.PatchSshCommand;
import com.apgsga.microservice.patch.core.impl.jenkins.JenkinsClient;
import com.apgsga.patch.db.integration.api.PatchRdbms;
import com.apgsga.patch.db.integration.impl.NotifyDbParameters;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PatchSetupTask implements Runnable {

	protected static final Log LOGGER = LogFactory.getLog(PatchSetupTask.class.getName());

	public static Runnable create(CommandRunner jschSession, Patch patch, ArtifactDependencyResolver dependencyResolver,
								  JenkinsClient jenkinsPatchClient, PatchPersistence repo, PatchRdbms patchRdms, SetupParameter setupParams) {
		return new PatchSetupTask(jschSession, patch, dependencyResolver, jenkinsPatchClient, repo, patchRdms, setupParams);
	}

	private final CommandRunner jschSession;
	private final Patch patch;
	private final ArtifactDependencyResolver dependencyResolver;
	private final JenkinsClient jenkinsPatchClient;
	private final PatchPersistence repo;
	private final PatchRdbms patchRdms;
	private final SetupParameter setupParams;

	private PatchSetupTask(CommandRunner jschSession, Patch patch,
						   ArtifactDependencyResolver dependencyResolver, JenkinsClient jenkinsPatchClient, PatchPersistence repo, PatchRdbms patchRdbms, SetupParameter setupParams) {
		super();
		this.jschSession = jschSession;
		this.patch = patch;
		this.dependencyResolver = dependencyResolver;
		this.jenkinsPatchClient = jenkinsPatchClient;
		this.repo = repo;
		this.patchRdms = patchRdbms;
		this.setupParams = setupParams;
	}

	@Override
	public void run() {
		try {
			LOGGER.info("Patch Setup Task started for patch " + patch.getPatchNumber());
			jschSession.preProcess();
			if (!patch.getDbObjectsAsVcsPath().isEmpty()) {
				LOGGER.info("Creating Tag for DB Objects for patch " + patch.getPatchNumber());
				jschSession.run(PatchSshCommand.createTagPatchModulesCmd(patch.getPatchTag(), patch.getDbPatchBranch(),
						patch.getDbObjectsAsVcsPath()));
			}
			for (Service service : patch.getServices()) {
				if (!service.getMavenArtifactsAsVcsPath().isEmpty()) {
					LOGGER.info("Creating Tag for Java Artifact for patch " + patch.getPatchNumber() + " and service " + service.getServiceName());
					jschSession.run(PatchSshCommand.createTagPatchModulesCmd(patch.getPatchTag(), service.getMicroServiceBranch(),
							service.getMavenArtifactsAsVcsPath()));
				}
			}
			jschSession.postProcess();
			for (Service service : patch.getServices()) {
				LOGGER.info("Resolving Java Artifact dependencies for patch " + patch.getPatchNumber() + " and service " + service.getServiceName());
				dependencyResolver.resolveDependencies(service.getArtifactsToPatch());
			}
			repo.savePatch(patch);
			LOGGER.info("Patch Setup Task started for patch " + patch.getPatchNumber() + " DONE !! -> notifying db accordingly!");
			patchRdms.notifyDb(NotifyDbParameters.create(patch.getPatchNumber()).successNotification(setupParams.getSuccessNotification()));
		}
		catch(Exception e) {
			LOGGER.error("Patch Setup Task for patch " + patch.getPatchNumber() + " encountered an error :" + e.getMessage());
			patchRdms.notifyDb(NotifyDbParameters.create(patch.getPatchNumber()).errorNotification(setupParams.getErrorNotification()));
		}
	}
}
