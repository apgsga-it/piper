package com.apgsga.microservice.patch.server.impl.jenkins;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.JenkinsTriggerHelper;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildResult;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.PendingInputActions;
import com.offbytwo.jenkins.model.PipelineBuild;
import com.offbytwo.jenkins.model.PipelineJobWithDetails;
import com.offbytwo.jenkins.model.WorkflowRun;

public class JenkinsClientImpl implements JenkinsClient {

	protected final Log LOGGER = LogFactory.getLog(getClass());

	private String jenkinsUrl;
	private String jenkinsUser;
	private String jenkinsUserAuthKey;

	public JenkinsClientImpl(String jenkinsUrl, String jenkinsUser, String jenkinsUserAuthKey) {
		super();
		this.jenkinsUrl = jenkinsUrl;
		this.jenkinsUser = jenkinsUser;
		this.jenkinsUserAuthKey = jenkinsUserAuthKey;
	}

	@Override
	public void createPatchPipelines(Patch patch) {
		// TODO (che. jhe ) : What happens , when the job fails? And when
		// somebody is
		// really fast with a patch?
		// TODO (che, 31.5 ) : The following thrown exceptions will not reach
		// the Client
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.execute(new Runnable() {

			@Override
			public void run() {
				try {
					JenkinsServer jenkinsServer = new JenkinsServer(new URI(jenkinsUrl), jenkinsUser,
							jenkinsUserAuthKey);
					JenkinsTriggerHelper jth = new JenkinsTriggerHelper(jenkinsServer, 2000L);
					Map<String, String> jobParm = Maps.newHashMap();
					jobParm.put("token", "PATCHBUILDER_START");
					jobParm.put("patchnumber", patch.getPatchNummer());
					BuildWithDetails patchBuilderResult = jth.triggerJobAndWaitUntilFinished("PatchBuilder", jobParm,
							true);
					if (!patchBuilderResult.getResult().equals(BuildResult.SUCCESS)) {
						LOGGER.error("PatchBuilder failed: " + patchBuilderResult.getResult().toString());
						throw ExceptionFactory.createPatchServiceRuntimeException(
								"JenkinsPatchClientImpl.createPatchPipelines.error",
								new Object[] { patch.toString(), patchBuilderResult.getResult().toString() });
					}
					LOGGER.info(patchBuilderResult.getConsoleOutputText().toString());
				} catch (Throwable e) {
					throw ExceptionFactory.createPatchServiceRuntimeException(
							"JenkinsPatchClientImpl.createPatchPipelines.exception",
							new Object[] { e.getMessage(), patch.toString() }, e);
				}
			}
		});
		executorService.shutdown();

	}

	@Override
	public void startInstallPipeline(Patch patch) {
		startPipeline(patch, "Download");

	}

	@Override
	public void startProdPatchPipeline(Patch patch) {
		startPipeline(patch, "");

	}

	private void startPipeline(Patch patch, String jobSuffix) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			String jsonRequestString = mapper.writeValueAsString(patch);
			LOGGER.info("Jenkins request: " + jsonRequestString.toString());
			String jobName = "Patch" + patch.getPatchNummer() + jobSuffix;

			JenkinsServer jenkinsServer = new JenkinsServer(new URI(jenkinsUrl), jenkinsUser, jenkinsUserAuthKey);
			LOGGER.info("Connected to Jenkinsserver with, url: " + jenkinsUrl + " and user: " + jenkinsUser);
			JenkinsTriggerHelper jth = new JenkinsTriggerHelper(jenkinsServer, 2000L);
			Map<String, String> jobParm = Maps.newHashMap();
			jobParm.put("token", jobName);
			jobParm.put("PARAMETER", jsonRequestString);
			// TODO (jhe , che , 12.12. 2017) : hangs, when job fails
			// immediately
			LOGGER.info("Triggering Pipeline Job and waiting until Building " + jobName + " with Paramter: "
					+ jobParm.toString());
			PipelineBuild result = jth.triggerPipelineJobAndWaitUntilBuilding(jobName, jobParm, true);
			LOGGER.info("Getting Result of Pipeline Job " + jobName + ", : " + result.toString());
			BuildWithDetails details = result.details();
			if (details.isBuilding()) {
				LOGGER.info(jobName + " Is Building");
				LOGGER.info("Buildnumber: " + details.getNumber());
			} else {
				throw ExceptionFactory.createPatchServiceRuntimeException("JenkinsPatchClientImpl.startPipeline.error",
						new Object[] { patch.toString(), jobName, details.getConsoleOutputText() });
			}

		} catch (URISyntaxException | IOException | InterruptedException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("JenkinsPatchClientImpl.startPipeline.exception",
					new Object[] { e.getMessage(), patch.toString() }, e);
		}
	}

	@Override
	public void cancelPatchPipeline(Patch patch) {
		processInputAction(patch, "cancel", null);
	}

	private PipelineBuild getPipelineBuild(JenkinsServer jenkinsServer, String patchNumber) throws IOException {
		String patchJobName = "Patch" + patchNumber;
		PipelineJobWithDetails job = jenkinsServer.getPipelineJob(patchJobName);
		PipelineBuild lastBuild = job.getLastBuild();
		return lastBuild;
	}

	@Override
	public void processInputAction(Patch patch, Map<String, String> parameter) {
		// TODO (che, 25.4) : Switch to logical Target name
		processInputAction(patch, parameter.get("target"), parameter.get("stage"));
	}

	@Override
	public void processInputAction(Patch patch, String targetName, String stage) {
		String action = stage.equals("cancel") ? stage : "Patch" + patch.getPatchNummer() + stage + targetName + "Ok";
		JenkinsServer jenkinsServer = null;
		try {
			jenkinsServer = new JenkinsServer(new URI(jenkinsUrl), jenkinsUser, jenkinsUserAuthKey);
			PipelineBuild lastBuild = getPipelineBuild(jenkinsServer, patch.getPatchNummer());
			validateLastPipelineBuilder(patch.getPatchNummer(), lastBuild);
			inputActionForPipeline(patch, action, lastBuild);

		} catch (URISyntaxException | IOException | InterruptedException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("JenkinsPatchClientImpl.processInputAction.exception",
					new Object[] { e.getMessage(), patch.toString() }, e);
		} finally { 
			if (jenkinsServer != null) {
				jenkinsServer.close();
			}
		}
	}

	private void inputActionForPipeline(Patch patch, String action, PipelineBuild lastBuild)
			throws IOException, InterruptedException {
		while (true) {
			WorkflowRun wfRun = lastBuild.getWorkflowRun();
			LOGGER.info("Workflow status: " + wfRun.getStatus().toString());
			if (!wfRun.isPausedPendingInput() && !wfRun.isInProgress()) {
				LOGGER.warn("Pipeline  : " + wfRun.getName() + " is not building");
				break;
			} else if (wfRun.isInProgress()) {
				// TODO (che, 28.12 ) : do what
				Thread.sleep(1000);
			} else {
				boolean actionFound = false;
				List<PendingInputActions> pendingInputActions = lastBuild.getPendingInputActions();
				for (PendingInputActions inputAction : pendingInputActions) {
					if ("cancel".equals(action)) {
						inputAction.abort();
						actionFound = true;
					}
					if (inputAction.getId().equals(action)) {
						actionFound = true;
						inputAction.proceed();
						break;
					}
				}
				if (!actionFound) {
					// TODO (che, 28.12 ) : specific Exception
					throw ExceptionFactory.createPatchServiceRuntimeException(
							"JenkinsPatchClientImpl.inputActionForPipeline.error",
							new Object[] { patch.toString(), action });
				}
				break;
			}
		}
	}

	private void validateLastPipelineBuilder(String patchNumber, PipelineBuild lastBuild) throws IOException {
		if (lastBuild == null || lastBuild.equals(Build.BUILD_HAS_NEVER_RUN)
				|| lastBuild.equals(Build.BUILD_HAS_BEEN_CANCELLED)) {
			// TODO (che, 28.12 ) : do what
			LOGGER.warn("Job with patchNumber: " + patchNumber + " is not building");
			return;
		}
		BuildWithDetails buildDetails = lastBuild.details();
		if (buildDetails == null || !buildDetails.isBuilding()) {
			// TODO (che, 28.12 ) : do what
			LOGGER.warn("Job with patchNumber: " + patchNumber + " is not building");
			return;
		}
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