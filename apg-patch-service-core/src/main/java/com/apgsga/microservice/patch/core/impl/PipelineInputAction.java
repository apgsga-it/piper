package com.apgsga.microservice.patch.core.impl;

import java.util.Map;

import com.apgsga.microservice.patch.api.PatchSystemMetaInfoPersistence;
import com.apgsga.microservice.patch.api.StageMapping;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.exceptions.Asserts;
import com.apgsga.microservice.patch.core.impl.jenkins.JenkinsClient;

public class PipelineInputAction implements PatchAction {
	protected static final Log LOGGER = LogFactory.getLog(PipelineInputAction.class.getName());

	private final PatchPersistence repo;
	private final PatchSystemMetaInfoPersistence metaInfoRepo;
	private final JenkinsClient jenkinsPatchClient;

	public PipelineInputAction(SimplePatchContainerBean patchContainer) {
		this.repo = patchContainer.getRepo();
		this.metaInfoRepo = patchContainer.getMetaInfoRepo();
		this.jenkinsPatchClient = patchContainer.getJenkinsClient();
	}


	// TODO JHE (11.11.2020): eventually we want to rename this ??
	@Override
	public String executeToStateAction(String patchNumber, String toAction, Map<String, String> parameter) {
		LOGGER.info("Running PipelineInputAction, with: " + patchNumber + ", " + toAction + ", and parameters: "
				+ parameter.toString());
		Patch patch = repo.findById(patchNumber);
		Asserts.notNull(patch, "PipelineInputAction.patch.exists.assert", new Object[] { patchNumber, toAction });
		StageMapping stage = metaInfoRepo.stageMappingFor(toAction);
		Asserts.notNull(stage,"PipelineInputAction.stage.exists.assert", new Object[] {toAction});
		jenkinsPatchClient.startBuildPatchPipeline(patch,stage);
		return "Ok: Executed for Patch: " + patchNumber + ", toState: " + toAction + ", with parameters: "
				+ parameter.toString();
	}

}
