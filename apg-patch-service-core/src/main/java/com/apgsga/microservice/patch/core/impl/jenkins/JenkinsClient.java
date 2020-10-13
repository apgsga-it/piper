package com.apgsga.microservice.patch.core.impl.jenkins;

import com.apgsga.microservice.patch.api.Patch;

import java.util.Map;

public interface JenkinsClient {

	void createPatchPipelines(Patch patch);

	void startProdPatchPipeline(Patch patch);

	void processInputAction(Patch patch, Map<String, String> parameter);

	void processInputAction(Patch patch, String target, String stage);
	
	void startAssembleAndDeployPipeline(String target, String parameter);

	void startInstallPipeline(String target, String parameter);

}
