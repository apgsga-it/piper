package com.apgsga.microservice.patch.core.impl.jenkins;

import com.apgsga.microservice.patch.api.*;
import com.apgsga.microservice.patch.core.patch.conflicts.PatchConflict;

import java.util.List;

public interface JenkinsClient {

	void createPatchPipelines(Patch patch);

	void startProdBuildPatchPipeline(BuildParameter parameters);

	void startAssembleAndDeployPipeline(AssembleAndDeployParameters parameters);

	void startInstallPipeline(InstallParameters parameters);

	void startOnDemandPipeline(OnDemandParameter parameters);

	void startOnClonePipeline(OnCloneParameters parameters);

	void startNotificationForPatchConflictPipeline(List<PatchListParameter> parameters, List<PatchConflict> patchConflicts);
}
