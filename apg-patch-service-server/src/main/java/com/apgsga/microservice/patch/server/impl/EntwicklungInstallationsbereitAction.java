package com.apgsga.microservice.patch.server.impl;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.util.Assert;

import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.server.impl.jenkins.JenkinsPatchClient;
import com.apgsga.microservice.patch.server.impl.vcs.PatchVcsCommand;
import com.apgsga.microservice.patch.server.impl.vcs.VcsCommandRunner;
import com.apgsga.microservice.patch.server.impl.vcs.VcsCommandRunnerFactory;

public class EntwicklungInstallationsbereitAction implements PatchAction {

	private final PatchPersistence repo;

	private VcsCommandRunnerFactory jschSessionFactory;

	private final JenkinsPatchClient jenkinsPatchClient;

	public EntwicklungInstallationsbereitAction(SimplePatchContainerBean patchContainer) {
		super();
		this.repo = patchContainer.getRepo();
		this.jschSessionFactory = patchContainer.getJschSessionFactory();
		this.jenkinsPatchClient = patchContainer.getJenkinsClient();
	}

	@Override
	public String executeToStateAction(String patchNumber, String toAction,  Map<String,String> parameter) {
		Patch patch = repo.findById(patchNumber);
		Assert.notNull(patch, "Patch : <" + patchNumber + "> not found");
		createAndSaveTagForPatch(patch);
		VcsCommandRunner jschSession = jschSessionFactory.create();
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.execute(new Runnable() {

			@Override
			public void run() {
				jschSession.preProcess();
				jschSession.run(PatchVcsCommand.createTagPatchModulesCmd(patch.getPatchTag(), patch.getProdBranch(),
						patch.getDbObjectsAsVcsPath()));
				jschSession.run(PatchVcsCommand.createTagPatchModulesCmd(patch.getPatchTag(),
						patch.getMicroServiceBranch(), patch.getMavenArtifactsAsVcsPath()));
				jschSession.postProcess();
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
