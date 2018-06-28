package com.apgsga.microservice.patch.server.impl.jenkins;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.google.common.collect.Maps;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.JenkinsTriggerHelper;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.PipelineBuild;

public class JenkinsAdminClientImpl implements JenkinsAdminClient {

	protected final Log LOGGER = LogFactory.getLog(getClass());

	private String jenkinsUrl;
	private String jenkinsUser;
	private String jenkinsUserAuthKey;

	public JenkinsAdminClientImpl(String jenkinsUrl, String jenkinsUser, String jenkinsUserAuthKey) {
		super();
		this.jenkinsUrl = jenkinsUrl;
		this.jenkinsUser = jenkinsUser;
		this.jenkinsUserAuthKey = jenkinsUserAuthKey;
	}
	
	@Override
	public void onClone(String target) {
		String jobName = "onClone";
		
		LOGGER.info("Starting onClone process for " + target + ". " + jobName + " pipeline will be started.");
		
		try {
			JenkinsServer jenkinsServer = new JenkinsServer(new URI(jenkinsUrl), jenkinsUser, jenkinsUserAuthKey);
			LOGGER.info("Connected to Jenkinsserver with, url: " + jenkinsUrl + " and user: " + jenkinsUser);
			JenkinsTriggerHelper jth = new JenkinsTriggerHelper(jenkinsServer, 2000L);
			Map<String, String> jobParm = Maps.newHashMap();
			jobParm.put("token", jobName);
			jobParm.put("target", target);
			PipelineBuild result = jth.triggerPipelineJobAndWaitUntilBuilding(jobName, jobParm, true);
			BuildWithDetails details = result.details();
			if (details.isBuilding()) {
				LOGGER.info(jobName + " Is Building");
				LOGGER.info("Buildnumber: " + details.getNumber());
			} else {
				throw ExceptionFactory.createPatchServiceRuntimeException("JenkinsAdminClientImpl.startPipeline.error",
						new Object[] { target, jobName, details.getConsoleOutputText() });
			}
		} catch (URISyntaxException | IOException | InterruptedException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("JenkinsAdminClientImpl.startPipeline.error",
					new Object[] { e.getMessage(), target }, e);
		}
		
	}

}
