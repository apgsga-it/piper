package com.apgsga.microservice.patch.server.impl.jenkins;

import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.TargetSystemEnviroment;

public interface JenkinsPatchClient {
	public void createPatchPipelines(Patch patch);
	public void startInstallPipeline(Patch patch);
	public void startProdPatchPipeline(Patch patch); 
	public void cancelPatchPipeline(Patch patch);
    public void approveBuild(TargetSystemEnviroment target,Patch patch);
	public void approveInstallation(TargetSystemEnviroment target,Patch patch);
	
}
