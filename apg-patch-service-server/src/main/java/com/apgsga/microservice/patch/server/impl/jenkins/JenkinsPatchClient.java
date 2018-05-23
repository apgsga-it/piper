package com.apgsga.microservice.patch.server.impl.jenkins;

import java.util.Map;

import com.apgsga.microservice.patch.api.Patch;

public interface JenkinsPatchClient {
	public void createPatchPipelines(Patch patch);

	public void startInstallPipeline(Patch patch);

	public void startProdPatchPipeline(Patch patch);

	public void cancelPatchPipeline(Patch patch);

	public void processInputAction(Patch patch, Map<String, String> parameter);

	public void processInputAction(Patch patch, String target, String stage);

}
