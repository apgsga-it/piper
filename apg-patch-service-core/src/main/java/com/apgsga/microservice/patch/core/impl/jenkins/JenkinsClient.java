package com.apgsga.microservice.patch.core.impl.jenkins;

import java.util.Map;

import com.apgsga.microservice.patch.api.Patch;

public interface JenkinsClient {
	public void createPatchPipelines(Patch patch);

	public void startProdPatchPipeline(Patch patch);

	public void restartProdPatchPipeline(Patch patch);

	public void cancelPatchPipeline(Patch patch);

	public void processInputAction(Patch patch, Map<String, String> parameter);

	public void processInputAction(Patch patch, String target, String stage);
	
	public void onClone(String source, String target);
	
	public boolean isProdPatchPipelineRunning(String patchNumber);

	// TODO JHE (24.08.2020): not sure we want a String here .... just that it compiles for first set of refactoring
	public String getProdPipelineBuildResult(String patchNumber);

	public void startAssembleAndDeployPipeline(String target);

	public void startInstallPipeline(String target);
}
