package com.apgsga.microservice.patch.core.impl.jenkins;

import com.apgsga.microservice.patch.api.*;
import com.apgsga.microservice.patch.core.patch.conflicts.PatchConflict;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Profile("mock")
@Component("jenkinsBean")
public class JenkinsMockClient implements JenkinsClient {

	protected final Log LOGGER = LogFactory.getLog(getClass());

	@Override
	public void createPatchPipelines(Patch patch) {
		LOGGER.info("createPatchPipelines for : " + patch.toString());
	}

	@Override
	public void startProdBuildPatchPipeline(BuildParameter bp) {
		LOGGER.info("Start build Pipeline for " + bp.toString());
	}

	@Override
	public void startAssembleAndDeployPipeline(AssembleAndDeployParameters parameters) {
		LOGGER.info("startAssembleAndDeployPipeline for : " + parameters.toString());
	}

	@Override
	public void startInstallPipeline(InstallParameters parameters) {
		LOGGER.info("startInstallPipeline for target=" + parameters.toString());
	}

	@Override
	public void startOnDemandPipeline(OnDemandParameter parameters) {
		LOGGER.info("startOnDemandPipeline for : " + parameters.toString());
	}

	@Override
	public void startOnClonePipeline(OnCloneParameters parameters) {
		LOGGER.info("startOnClonePipeline for : " + parameters.toString());
	}

	@Override
	public void startNotificationForPatchConflictPipeline(List<PatchListParameter> patchListParams, List<PatchConflict> patchConflicts) {
		LOGGER.info("startNotificationForPatchConflictPipeline for : ");
		patchListParams.forEach(p -> {
			LOGGER.info(p.toString());
		});
	}

}
