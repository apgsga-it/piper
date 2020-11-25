package com.apgsga.microservice.patch.core.impl.jenkins;

import com.apgsga.microservice.patch.api.BuildParameter;
import com.apgsga.microservice.patch.api.Patch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("mock")
@Component("jenkinsBean")
public class JenkinsMockClient implements JenkinsClient {

	protected final Log LOGGER = LogFactory.getLog(getClass());

	@Override
	public void createPatchPipelines(Patch patch) {
		LOGGER.info("createPatchPipelines for : " + patch.toString());
	}

	@Override
	public void startProdBuildPatchPipeline(BuildParameter bp) {
		LOGGER.info("Start build Pipeline for " + bp.toString());
	}

	@Override
	public void startAssembleAndDeployPipeline(String target, String parameter) {
		LOGGER.info("startAssembleAndDeployPipeline for target=" + target);
	}

	@Override
	public void startInstallPipeline(String target, String parameter) {
		LOGGER.info("startInstallPipeline for target=" + target);
	}


}
