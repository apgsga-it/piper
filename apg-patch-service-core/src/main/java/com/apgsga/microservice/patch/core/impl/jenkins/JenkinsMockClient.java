package com.apgsga.microservice.patch.core.impl.jenkins;

import com.apgsga.microservice.patch.api.Patch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

public class JenkinsMockClient implements JenkinsClient {

	protected final Log LOGGER = LogFactory.getLog(getClass());

	@Override
	public void createPatchPipelines(Patch patch) {
		LOGGER.info("createPatchPipelines for : " + patch.toString());
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
		LOGGER.info("processInputAction for patch: " + patch.toString() + ",with parameters: " + parameter.toString());

	}

	@Override
	public void startAssembleAndDeployPipeline(String target) {
		LOGGER.info("startAssembleAndDeployPipeline for target=" + target);
	}

	@Override
	public void startInstallPipeline(String target) {
		LOGGER.info("startInstallPipeline for target=" + target);
	}

	@Override
	public void startJenkinsJob(String jobName) {
		LOGGER.info("startJenkinsJob, jobName=" + jobName);
	}

	@Override
	public void startJenkinsJob(String jobName, Map<String,String> params) {
		LOGGER.info("startJenkinsjob, jobName=" + jobName);
		if(params != null && !params.isEmpty()) {
			params.keySet().forEach(k -> {
				LOGGER.info("Job Param key = " + k + ", value = " + params.get(k));
			});
		}
	}
}
