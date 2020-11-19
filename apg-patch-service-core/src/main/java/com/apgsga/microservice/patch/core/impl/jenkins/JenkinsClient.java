package com.apgsga.microservice.patch.core.impl.jenkins;

import com.apgsga.microservice.patch.api.Patch;

public interface JenkinsClient {

	void createPatchPipelines(Patch patch);

	void startProdBuildPatchPipeline(Patch patch, String stage, String target, String successNotification);

	void startAssembleAndDeployPipeline(String target, String parameter);

	void startInstallPipeline(String target, String parameter);
}
