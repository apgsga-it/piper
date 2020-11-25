package com.apgsga.microservice.patch.core.impl.jenkins;

import com.apgsga.microservice.patch.api.BuildParameter;
import com.apgsga.microservice.patch.api.Patch;

public interface JenkinsClient {

	void createPatchPipelines(Patch patch);

	void startProdBuildPatchPipeline(BuildParameter parameters, String target);

	void startAssembleAndDeployPipeline(String target, String parameter);

	void startInstallPipeline(String target, String parameter);
}
