package com.apgsga.microservice.patch.core.impl.jenkins;

import com.apgsga.microservice.patch.api.AssembleAndDeployParameters;
import com.apgsga.microservice.patch.api.BuildParameter;
import com.apgsga.microservice.patch.api.InstallParameters;
import com.apgsga.microservice.patch.api.Patch;

public interface JenkinsClient {

	void createPatchPipelines(Patch patch);

	void startProdBuildPatchPipeline(BuildParameter parameters);

	void startAssembleAndDeployPipeline(AssembleAndDeployParameters parameters);

	void startInstallPipeline(InstallParameters parameters);
}
