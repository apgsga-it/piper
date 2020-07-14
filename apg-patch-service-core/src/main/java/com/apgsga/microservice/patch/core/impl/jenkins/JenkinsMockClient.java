package com.apgsga.microservice.patch.core.impl.jenkins;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.apgsga.microservice.patch.api.Patch;
import com.offbytwo.jenkins.model.BuildResult;

public class JenkinsMockClient implements JenkinsClient {

	protected final Log LOGGER = LogFactory.getLog(getClass());

	@Override
	public void createPatchPipelines(Patch patch) {
		LOGGER.info("createPatchPipelines for : " + patch.toString());
	}

	@Override
	public void startProdPatchPipeline(Patch patch) {
		LOGGER.info("startProdPatchPipeline for : " + patch.toString());

	}
	
	@Override
	public void restartProdPatchPipeline(Patch patch) {
		LOGGER.info("restartProdPatchPipeline for : " + patch.toString() );
		
	}

	@Override
	public void processInputAction(Patch patch, String target, String stage) {
		LOGGER.info("approvate for target " + target + ",stage: " + stage + " and patch: " + patch.toString());

	}

	@Override
	public void processInputAction(Patch patch, Map<String, String> parameter) {
		LOGGER.info("processInputAction for patch: " + patch.toString() + ",with parameters: " + parameter.toString());

	}

	@Override
	public void cancelPatchPipeline(Patch patch) {
		LOGGER.info("cancelPatchPipeline for : " + patch.toString());

	}
	
	@Override
	public void onClone(String source, String target) {
		LOGGER.info("onClone for source=" + source + " , target=" + target);		
	}

	@Override
	public boolean isProdPatchPipelineRunning(String patchNumber) {
		LOGGER.info("isProdPatchPipelineRunning for :" + patchNumber);
		return false;
	}

	@Override
	public BuildResult getProdPipelineBuildResult(String patchNumber) {
		LOGGER.info("getProdPipelineBuildResult for : " + patchNumber);
		return BuildResult.ABORTED;
	}

	@Override
	public void startAssembleAndDeployPipeline(String target) {
		LOGGER.info("startAssembleAndDeployPipeline for target=" + target);
	}

	@Override
	public void startInstallPipeline(String target) {
		LOGGER.info("startInstallPipeline for target=" + target);
	}
}
