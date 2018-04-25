package com.apgsga.microservice.patch.server.impl;

import java.util.Map;

import org.springframework.util.Assert;

import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.api.TargetSystemEnviroment;
import com.apgsga.microservice.patch.server.impl.jenkins.JenkinsPatchClient;

public class PipelineInputAction implements PatchAction {

	private final PatchPersistence repo;
	private final JenkinsPatchClient jenkinsPatchClient;

	public PipelineInputAction(SimplePatchContainerBean patchContainer) {
		this.repo = patchContainer.getRepo();
		this.jenkinsPatchClient = patchContainer.getJenkinsClient();
	}
	
	

	@Override
	public String executeToStateAction(String patchNumber, String toAction, Map<String,String> parameter) {
		Patch patch = repo.findById(patchNumber);
		Assert.notNull(patch, "Patch : " + patchNumber + " not found");
		jenkinsPatchClient.processInputAction(patch, parameter);
		return "Ok: Executed for Patch: " + patchNumber + ", toState: " + toAction + ", with parameters: " + parameter.toString();
	}

}
