package com.apgsga.microservice.patch.core.impl.jenkins;

import com.apgsga.microservice.patch.api.*;

public interface JenkinsClient {

	void createPatchPipelines(Patch patch);

	void startProdBuildPatchPipeline(BuildParameter parameters);

	void startAssembleAndDeployPipeline(AssembleAndDeployParameters parameters);

	void startInstallPipeline(InstallParameters parameters);

	void startOnDemandPipeline(OnDemandParameter parameters);

	void startOnClonePipeline(OnCloneParameters parameters);
}
