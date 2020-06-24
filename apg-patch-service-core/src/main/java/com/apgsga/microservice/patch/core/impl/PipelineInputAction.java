package com.apgsga.microservice.patch.core.impl;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.exceptions.Asserts;
import com.apgsga.microservice.patch.core.impl.jenkins.JenkinsClient;

public class PipelineInputAction implements PatchAction {
	protected static final Log LOGGER = LogFactory.getLog(PipelineInputAction.class.getName());

	private final PatchPersistence repo;
	private final JenkinsClient jenkinsPatchClient;

	public PipelineInputAction(SimplePatchContainerBean patchContainer) {
		this.repo = patchContainer.getRepo();
		this.jenkinsPatchClient = patchContainer.getJenkinsClient();
	}

	@Override
	public String executeToStateAction(String patchNumber, String toAction, Map<String, String> parameter) {
		LOGGER.info("Running PipelineInputAction, with: " + patchNumber + ", " + toAction + ", and parameters: "
				+ parameter.toString());
		Patch patch = repo.findById(patchNumber);
		Asserts.notNull(patch, "PipelineInputAction.patch.exists.assert", new Object[] { patchNumber, toAction });
		jenkinsPatchClient.processInputAction(patch, parameter);
		return "Ok: Executed for Patch: " + patchNumber + ", toState: " + toAction + ", with parameters: "
				+ parameter.toString();
	}

}