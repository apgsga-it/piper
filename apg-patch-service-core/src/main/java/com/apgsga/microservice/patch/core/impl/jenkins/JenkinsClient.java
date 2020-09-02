package com.apgsga.microservice.patch.core.impl.jenkins;

import java.util.Map;

import com.apgsga.microservice.patch.api.Patch;

public interface JenkinsClient {
	public void createPatchPipelines(Patch patch);

	public void startProdPatchPipeline(Patch patch);

	public void processInputAction(Patch patch, Map<String, String> parameter);

	public void processInputAction(Patch patch, String target, String stage);
	
	public void startAssembleAndDeployPipeline(String target);

	public void startInstallPipeline(String target);
}
