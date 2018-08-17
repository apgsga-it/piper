package com.apgsga.microservice.patch.server.impl;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.apgsga.artifact.query.ArtifactDependencyResolver;
import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.exceptions.Asserts;
import com.apgsga.microservice.patch.server.impl.jenkins.JenkinsClient;
import com.apgsga.microservice.patch.server.impl.vcs.PatchVcsCommand;
import com.apgsga.microservice.patch.server.impl.vcs.VcsCommandRunner;
import com.apgsga.microservice.patch.server.impl.vcs.VcsCommandRunnerFactory;

public class EntwicklungInstallationsbereitAction implements PatchAction {
	protected final Log LOGGER = LogFactory.getLog(getClass());

	private final PatchPersistence repo;

	private VcsCommandRunnerFactory jschSessionFactory;

	private final JenkinsClient jenkinsPatchClient;
	
	private final ArtifactDependencyResolver dependencyResolver; 

	public EntwicklungInstallationsbereitAction(SimplePatchContainerBean patchContainer) {
		super();
		this.repo = patchContainer.getRepo();
		this.jschSessionFactory = patchContainer.getJschSessionFactory();
		this.jenkinsPatchClient = patchContainer.getJenkinsClient();
		this.dependencyResolver = patchContainer.getDependecyResolver();
	}

	@Override
	public String executeToStateAction(String patchNumber, String toAction, Map<String, String> parameter) {
		LOGGER.info("Running EntwicklungInstallationsbereitAction, with: " + patchNumber + ", " + toAction
				+ ", and parameters: " + parameter.toString());
		Patch patch = repo.findById(patchNumber);
		Asserts.notNull(patch, "EntwicklungInstallationsbereitAction.patch.exists.assert",
				new Object[] { patchNumber, toAction });
		createAndSaveTagForPatch(patch);
		VcsCommandRunner jschSession = jschSessionFactory.create();
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.execute(new Runnable() {

			@Override
			public void run() {
				LOGGER.info("Running EntwicklungInstallationsbereitAction PatchVcsCommands");
				jschSession.preProcess();
				if(!patch.getDbObjectsAsVcsPath().isEmpty()) {
					jschSession.run(PatchVcsCommand.createTagPatchModulesCmd(patch.getPatchTag(), patch.getDbPatchBranch(),
							patch.getDbObjectsAsVcsPath()));
				}
				if(!patch.getMavenArtifactsAsVcsPath().isEmpty()) {
					jschSession.run(PatchVcsCommand.createTagPatchModulesCmd(patch.getPatchTag(),
							patch.getMicroServiceBranch(), patch.getMavenArtifactsAsVcsPath()));
				}
				jschSession.postProcess();
				dependencyResolver.resolveDependencies(patch.getMavenArtifacts());
				repo.savePatch(patch);
				LOGGER.info("Running EntwicklungInstallationsbereitAction startProdPatchPipeline");
				jenkinsPatchClient.startProdPatchPipeline(patch);
			}

		});
		executorService.shutdown();
		return "Ok: Created Patch Tag and started Prod Patch Pipeline for: " + patch.getPatchNummer();
	}

	private void createAndSaveTagForPatch(Patch patch) {
		patch.incrementTagNr();
		Integer tagNr = patch.getTagNr();
		String patchBranch = patch.getDbPatchBranch();
		patch.setPatchTag(patchBranch + "_" + tagNr.toString());
		repo.savePatch(patch);
	}

}
