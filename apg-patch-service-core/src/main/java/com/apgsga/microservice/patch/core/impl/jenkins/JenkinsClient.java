package com.apgsga.microservice.patch.core.impl.jenkins;

import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.StageMapping;

public interface JenkinsClient {

	void createPatchPipelines(Patch patch);

	void startBuildPatchPipeline(Patch patch, StageMapping stage);

	void startAssembleAndDeployPipeline(String target, String parameter);

	void startInstallPipeline(String target, String parameter);
}
