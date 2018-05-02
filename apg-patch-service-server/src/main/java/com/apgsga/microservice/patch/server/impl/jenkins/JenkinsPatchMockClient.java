package com.apgsga.microservice.patch.server.impl.jenkins;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.TargetSystemEnviroment;

public class JenkinsPatchMockClient implements JenkinsPatchClient {

	protected final Log LOGGER = LogFactory.getLog(getClass());

	@Override
	public void createPatchPipelines(Patch patch) {
		LOGGER.info("createPatchPipelines for : " + patch.toString());
	}

	@Override
	public void startInstallPipeline(Patch patch) {
		LOGGER.info("startInstallPipeline for : " + patch.toString());

	}

	@Override
	public void startProdPatchPipeline(Patch patch) {
		LOGGER.info("startProdPatchPipeline for : " + patch.toString());

	}

	@Override
	public void processInputAction(Patch patch, String target, String stage) {
		LOGGER.info("approvate for target " + target + ",stage: " + stage + " and patch: " + patch.toString());

	}
	
	

	@Override
	public void processInputAction(Patch patch, Map<String, String> parameter) {
		LOGGER.info("processInputAction for patch: " + patch.toString()  + ",with parameters: " + parameter.toString());

	}

	@Override
	public void cancelPatchPipeline(Patch patch) {
		LOGGER.info("cancelPatchPipeline for : " + patch.toString());

	}

	@Override
	public void approveBuild(TargetSystemEnviroment target, Patch patch) {
		LOGGER.info("approveBuild for : " + patch.toString() + " and Target: " + target);

	}

	@Override
	public void approveInstallation(TargetSystemEnviroment target, Patch patch) {
		LOGGER.info("approveInstallation for : " + patch.toString() + " and Target: " + target);

	}

}
