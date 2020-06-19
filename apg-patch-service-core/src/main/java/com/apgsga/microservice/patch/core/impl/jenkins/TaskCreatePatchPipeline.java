package com.apgsga.microservice.patch.core.impl.jenkins;

import java.net.URI;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.google.common.collect.Maps;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.JenkinsTriggerHelper;
import com.offbytwo.jenkins.model.BuildResult;
import com.offbytwo.jenkins.model.BuildWithDetails;

public class TaskCreatePatchPipeline implements Runnable {

	protected static final Log LOGGER = LogFactory.getLog(TaskCreatePatchPipeline.class.getName());
	
	public static Runnable create(String jenkinsUrl, String jenkinsUser, String jenkinsUserAuthKey, Patch patch) {
		return new TaskCreatePatchPipeline(jenkinsUrl, jenkinsUser, jenkinsUserAuthKey, patch); 
	}

	private String jenkinsUrl;
	private String jenkinsUser;
	private String jenkinsUserAuthKey;
	private Patch patch;
	

	private TaskCreatePatchPipeline(String jenkinsUrl, String jenkinsUser, String jenkinsUserAuthKey, Patch patch) {
		super();
		this.jenkinsUrl = jenkinsUrl;
		this.jenkinsUser = jenkinsUser;
		this.jenkinsUserAuthKey = jenkinsUserAuthKey;
		this.patch = patch;
	}

	@Override
	public void run() {
		try {
			JenkinsServer jenkinsServer = new JenkinsServer(new URI(jenkinsUrl), jenkinsUser, jenkinsUserAuthKey);
			JenkinsTriggerHelper jth = new JenkinsTriggerHelper(jenkinsServer, 2000L);
			Map<String, String> jobParm = Maps.newHashMap();
			jobParm.put("token", "PATCHBUILDER_START");
			jobParm.put("patchnumber", patch.getPatchNummer());
			BuildWithDetails patchBuilderResult = jth.triggerJobAndWaitUntilFinished("PatchBuilder", jobParm, true);
			if (!patchBuilderResult.getResult().equals(BuildResult.SUCCESS)) {
				LOGGER.error("PatchBuilder failed: " + patchBuilderResult.getResult().toString());
				throw ExceptionFactory.createPatchServiceRuntimeException(
						"JenkinsPatchClientImpl.createPatchPipelines.error",
						new Object[] { patch.toString(), patchBuilderResult.getResult().toString() });
			}
			LOGGER.info(patchBuilderResult.getConsoleOutputText().toString());
		} catch (Exception e) {
			throw ExceptionFactory.createPatchServiceRuntimeException(
					"JenkinsPatchClientImpl.createPatchPipelines.exception",
					new Object[] { e.getMessage(), patch.toString() }, e);
		}
	}

}
