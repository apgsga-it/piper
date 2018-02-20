package com.apgsga.microservice.patch.server.impl;

import org.springframework.util.Assert;

import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.server.impl.jenkins.JenkinsPatchClient;

public class ActionToEntwicklung implements ActionExecuteStateTransition {

	private final PatchPersistence repo;
	private final JenkinsPatchClient jenkinsPatchClient;

	public ActionToEntwicklung(SimplePatchContainerBean patchContainer) {
		this.repo = patchContainer.getRepo();
		this.jenkinsPatchClient = patchContainer.getJenkinsClient();
	}

	@Override
	public void executeStateTransitionAction(String patchNumber) {
		Patch patch = repo.findById(patchNumber);
		Assert.notNull(patch, "Patch : " + patchNumber + " not found");
		jenkinsPatchClient.cancelPatchPipeline(patch);
	}

}
