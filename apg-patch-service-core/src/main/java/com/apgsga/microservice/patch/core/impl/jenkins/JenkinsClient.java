package com.apgsga.microservice.patch.core.impl.jenkins;

import java.util.Map;

import com.apgsga.microservice.patch.api.Patch;
import com.offbytwo.jenkins.model.BuildResult;

public interface JenkinsClient {
	public void createPatchPipelines(Patch patch);

	public void startInstallPipeline(Patch patch);

	public void startProdPatchPipeline(Patch patch);

	public void restartProdPatchPipeline(Patch patch);

	public void cancelPatchPipeline(Patch patch);

	public void processInputAction(Patch patch, Map<String, String> parameter);

	public void processInputAction(Patch patch, String target, String stage);
	
	public void onClone(String source, String target);
	
	public boolean isProdPatchPipelineRunning(String patchNumber);
	
	public BuildResult getProdPipelineBuildResult(String patchNumber);

	public void startAssembleAndDeployPipeline(Map<String,String> params);
}
