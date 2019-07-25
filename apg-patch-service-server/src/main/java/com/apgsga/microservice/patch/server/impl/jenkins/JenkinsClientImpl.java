package com.apgsga.microservice.patch.server.impl.jenkins;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;

import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.apgsga.microservice.patch.exceptions.PatchServiceRuntimeException;
import com.google.common.collect.Maps;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildResult;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.PendingInputActions;
import com.offbytwo.jenkins.model.PipelineBuild;
import com.offbytwo.jenkins.model.PipelineJobWithDetails;
import com.offbytwo.jenkins.model.QueueItem;
import com.offbytwo.jenkins.model.QueueReference;
import com.offbytwo.jenkins.model.WorkflowRun;

public class JenkinsClientImpl implements JenkinsClient {

	private static final String OK_CONS = "Ok";

	private static final String PIPELINE_CONS = "Pipeline  : ";

	private static final String IS_NOT_BUILDING_CONS = " is not building";

	private static final String CANCEL_CONS = "cancel";

	private static final String TOKEN_CONS = "token";

	private static final String JSON_CONS = ".json";

	private static final String PATCH_CONS = "Patch";

	protected static final Log LOGGER = LogFactory.getLog(JenkinsClientImpl.class.getName());

	private static final Long DEFAULT_RETRY_INTERVAL = 200L;
	private static final int DEFAULT_RETRY_COUNTS = 6;
	private Resource dbLocation;
	private String jenkinsUrl;
	private String jenkinsUser;
	private String jenkinsUserAuthKey;
	private TaskExecutor threadExecutor;

	public JenkinsClientImpl(Resource dbLocation, String jenkinsUrl, String jenkinsUser, String jenkinsUserAuthKey,
			TaskExecutor taskExectuor) {
		super();
		this.dbLocation = dbLocation;
		this.jenkinsUrl = jenkinsUrl;
		this.jenkinsUser = jenkinsUser;
		this.jenkinsUserAuthKey = jenkinsUserAuthKey;
		this.threadExecutor = taskExectuor;
	}

	@Override
	public void createPatchPipelines(Patch patch) {
		threadExecutor.execute(TaskCreatePatchPipeline.create(jenkinsUrl, jenkinsUser, jenkinsUserAuthKey, patch));

	}

	@Override
	public void startInstallPipeline(Patch patch) {
		startPipeline(patch, "OnDemand", false);

	}

	@Override
	public void startProdPatchPipeline(Patch patch) {
		startPipeline(patch, "", false);

	}

	@Override
	public void restartProdPatchPipeline(Patch patch) {
		startPipeline(patch, "", true);

	}

	private void startPipeline(Patch patch, String jobSuffix, boolean restart) {
		try {
			String patchName = PATCH_CONS + patch.getPatchNummer();
			String jobName = patchName + jobSuffix;
			File patchFile = new File(dbLocation.getFile(), patchName + JSON_CONS);
			JenkinsServer jenkinsServer = new JenkinsServer(new URI(jenkinsUrl), jenkinsUser, jenkinsUserAuthKey);
			LOGGER.info("Connected to Jenkinsserver with, url: " + jenkinsUrl + " and user: " + jenkinsUser);
			Map<String, String> jobParm = Maps.newHashMap();
			jobParm.put(TOKEN_CONS, jobName);
			jobParm.put("PARAMETER", patchFile.getAbsolutePath());
			jobParm.put("RESTART", restart ? "TRUE" : "FALSE");

			LOGGER.info("Triggering Pipeline Job and waiting until Building " + jobName + " with Paramter: "
					+ jobParm.toString());
			if(jobSuffix.equalsIgnoreCase("ondemand")) {
				triggerPipelineJobWithoutWaitingOnFeedback(jenkinsServer, jobName, jobParm, true);
				LOGGER.info("ondemand job for patch " + patch.getPatchNummer() + " has been started. No post-submit verification will be done.");
			}
			else {
				PipelineBuild result = triggerPipelineJobAndWaitUntilBuilding(jenkinsServer, jobName, jobParm, true);	
				LOGGER.info("Getting Result of Pipeline Job " + jobName + ", : " + result.toString());
				BuildWithDetails details = result.details();
				if (details.isBuilding()) {
					LOGGER.info(jobName + " Is Building");
					LOGGER.info("Buildnumber: " + details.getNumber());
				} else {
					throw ExceptionFactory.createPatchServiceRuntimeException("JenkinsPatchClientImpl.startPipeline.error",
							new Object[] { patch.toString(), jobName, details.getConsoleOutputText() });
				}
			}

		} catch (Exception e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("JenkinsPatchClientImpl.startPipeline.exception",
					new Object[] { e.getMessage(), patch.toString() }, e);
		}
	}

	private synchronized void triggerPipelineJobWithoutWaitingOnFeedback(JenkinsServer jenkinsServer, String jobName, Map<String, String> jobParm, boolean crumbFlag) {
		try {
			PipelineJobWithDetails job = jenkinsServer.getPipelineJob(jobName);
			job.build(jobParm, crumbFlag);
		} catch (IOException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("JenkinsPatchClientImpl.triggerPipelineJobWithoutWaitingOnFeedback.exception", new Object[]{e.getMessage(), jobName});
		}
	}

	@Override
	public void cancelPatchPipeline(Patch patch) {
		processInputAction(patch, CANCEL_CONS, null);
	}

	private PipelineBuild getPipelineBuild(JenkinsServer jenkinsServer, String jobName) throws IOException {
		PipelineJobWithDetails job = jenkinsServer.getPipelineJob(jobName);
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
		String action = stage.equals(CANCEL_CONS) ? stage
				: PATCH_CONS + patch.getPatchNummer() + stage + targetName + OK_CONS;
		JenkinsServer jenkinsServer = null;
		try {
			jenkinsServer = new JenkinsServer(new URI(jenkinsUrl), jenkinsUser, jenkinsUserAuthKey);
			PipelineBuild lastBuild = getPipelineBuild(jenkinsServer, PATCH_CONS + patch.getPatchNummer());
			validateLastPipelineBuilder(patch.getPatchNummer(), lastBuild);
			inputActionForPipeline(patch, action, lastBuild);

		} catch (Exception e) {
			throw ExceptionFactory.createPatchServiceRuntimeException(
					"JenkinsPatchClientImpl.processInputAction.exception",
					new Object[] { e.getMessage(), patch.toString() }, e);
		} finally {
			if (jenkinsServer != null) {
				jenkinsServer.close();
			}
		}
	}

	private void inputActionForPipeline(Patch patch, String action, PipelineBuild lastBuild)
			throws IOException, InterruptedException {
		int i = 0; 
		while (waitForAndProcessInput(patch, action, lastBuild,i) && i < 10) {
			// Loops with condition

		}
	}

	private boolean waitForAndProcessInput(Patch patch, String action, PipelineBuild lastBuild, int loops)
			throws IOException, InterruptedException {
		WorkflowRun wfRun = lastBuild.getWorkflowRun();
		LOGGER.info("Workflow status: " + wfRun.getStatus());
		if (!wfRun.isPausedPendingInput() && !wfRun.isInProgress()) {
			LOGGER.warn(PIPELINE_CONS + wfRun.getName() + IS_NOT_BUILDING_CONS);
			return false;
		} else if (wfRun.isInProgress()) {
			Thread.sleep(2000);
			return true;
		} else {
			boolean actionFound = false;
			List<PendingInputActions> pendingInputActions = lastBuild.getPendingInputActions();
			for (PendingInputActions inputAction : pendingInputActions) {
				if (CANCEL_CONS.equals(action)) {
					inputAction.abort();
					actionFound = true;
				}
				if (inputAction.getId().equals(action)) {
					actionFound = true;
					inputAction.proceed();
				}
			}
			if (!actionFound) {
				throw ExceptionFactory.createPatchServiceRuntimeException(
						"JenkinsPatchClientImpl.inputActionForPipeline.error",
						new Object[] { patch.toString(), action });
			}
			return false;
		}
	}

	private void validateLastPipelineBuilder(String patchNumber, PipelineBuild lastBuild) throws IOException {
		if (lastBuild == null || lastBuild.equals(Build.BUILD_HAS_NEVER_RUN)
				|| lastBuild.equals(Build.BUILD_HAS_BEEN_CANCELLED)) {
			LOGGER.warn("Job with patchNumber: " + patchNumber + IS_NOT_BUILDING_CONS);
			return;
		}
		BuildWithDetails buildDetails = lastBuild.details();
		if (buildDetails == null || !buildDetails.isBuilding()) {
			LOGGER.warn("Job with patchNumber: " + patchNumber + IS_NOT_BUILDING_CONS);
		}
	}

	@Override
	public void onClone(String source, String target) {
		String jobName = "onClone" + target.toUpperCase();
		LOGGER.info("Starting onClone process for " + target + ". " + jobName + " pipeline will be started.");

		try {
			JenkinsServer jenkinsServer = new JenkinsServer(new URI(jenkinsUrl), jenkinsUser, jenkinsUserAuthKey);
			LOGGER.info("Connected to Jenkinsserver with, url: " + jenkinsUrl + " and user: " + jenkinsUser);
			Map<String, String> jobParm = Maps.newHashMap();
			jobParm.put(TOKEN_CONS, jobName);
			jobParm.put("target", target);
			jobParm.put("source", source);
			triggerPipelineJobWithoutWaitingOnFeedback(jenkinsServer, jobName, jobParm, true);
			onCloneJobLog(jenkinsServer,jobName);
		} catch (PatchServiceRuntimeException e) {
			throw e;

		} catch (Exception e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("JenkinsAdminClientImpl.startPipeline.error",
					new Object[] { e.getMessage(), target }, e);
		}

	}

	private void onCloneJobLog(JenkinsServer jenkinsServer, String jobName) throws IOException, InterruptedException {
		PipelineBuild onCloneBuild = getPipelineBuild(jenkinsServer, jobName);		
		final int MAX_RETRY = 10;
		int attempt = 0;
		final int WAIT_TIME_IN_SEC_BEFORE_NEXT_TRY = 2;
		while(attempt < MAX_RETRY && !onCloneBuild.details().isBuilding()) {
			attempt++;
			LOGGER.info("\"" + jobName + "\"" + " has not been started yet. Waiting " + WAIT_TIME_IN_SEC_BEFORE_NEXT_TRY + "sec before next check." + (MAX_RETRY-attempt) + " checks remained.");
			Thread.sleep(WAIT_TIME_IN_SEC_BEFORE_NEXT_TRY*1000);
		}
		if(attempt == MAX_RETRY) {
			LOGGER.info("We could not determine if \"" + jobName + "\" has been submitted.");
		}
		else {
			LOGGER.info("\"" + jobName + "\"" + " correctly submitted and now building.");
		}
	}

	private static synchronized PipelineBuild triggerPipelineJobAndWaitUntilBuilding(JenkinsServer server, String jobName,
			Map<String, String> params, boolean crumbFlag) throws IOException, InterruptedException {
		LOGGER.info("Getting Job Details for Job \"" + jobName + "\"");
		PipelineJobWithDetails job = server.getPipelineJob(jobName);
		QueueReference queueRef = job.build(params, crumbFlag);
		LOGGER.info("Returning Details for Job \"" + jobName + "\"");
		return triggerPipelineJobAndWaitUntilBuilding(server, jobName, queueRef);
	}

	private static PipelineBuild triggerPipelineJobAndWaitUntilBuilding(JenkinsServer server, String jobName,
			QueueReference queueRef) throws IOException, InterruptedException {
		LOGGER.info("Getting Job Details for Job \"" + jobName + "\"");
		PipelineJobWithDetails job = server.getPipelineJob(jobName);
		QueueItem queueItem = server.getQueueItem(queueRef);
		int retryCnt = 0;
		while (!queueItem.isCancelled() && (job.isInQueue() || queueItem.getExecutable() == null)
				&& retryCnt < DEFAULT_RETRY_COUNTS) {
			Thread.sleep(DEFAULT_RETRY_INTERVAL);
			LOGGER.info("... retry getting Job Details for Job \"" + jobName + "\" (" + retryCnt + "/" + DEFAULT_RETRY_COUNTS + ")");
			job = server.getPipelineJob(jobName);
			queueItem = server.getQueueItem(queueRef);
			retryCnt++;
		}
		if (retryCnt >= DEFAULT_RETRY_COUNTS) {
			throw ExceptionFactory.createPatchServiceRuntimeException(
					"JenkinsPatchClientImpl.triggerPipelineJobAndWaitUntilBuilding.error", new Object[] { jobName });
		}
		
		LOGGER.info("Waiting until Job \"" + jobName + "\" is building");
		Build build = server.getBuild(queueItem);
		retryCnt = 0;
		while (retryCnt < DEFAULT_RETRY_COUNTS) {
			BuildWithDetails buildWithDetails = build.details();
			if (buildWithDetails.isBuilding()) {
				job = server.getPipelineJob(jobName);
				return job.getLastBuild();
			}
			BuildResult buildResult = buildWithDetails.getResult();
			if (buildResult != null
					&& (buildResult.equals(BuildResult.ABORTED) || buildResult.equals(BuildResult.SUCCESS)
							|| buildResult.equals(BuildResult.CANCELLED) || buildResult.equals(BuildResult.UNSTABLE))) {
				job = server.getPipelineJob(jobName);
				LOGGER.info("Job \"" + jobName + "\" is now building");
				return job.getLastBuild();
			}
			Thread.sleep(DEFAULT_RETRY_INTERVAL);
			LOGGER.info("... continue waiting until Job \"" + jobName + "\" is building");
			retryCnt++;
		}
		throw ExceptionFactory.createPatchServiceRuntimeException(
				"JenkinsPatchClientImpl.triggerPipelineJobAndWaitUntilBuilding.error", new Object[] { jobName });
	}

	@Override
	public boolean isProdPatchPipelineRunning(String patchNumber) {
		JenkinsServer jenkinsServer;
		try {
			jenkinsServer = new JenkinsServer(new URI(jenkinsUrl), jenkinsUser, jenkinsUserAuthKey);
			PipelineBuild lastBuild = getPipelineBuild(jenkinsServer, PATCH_CONS + patchNumber);
			return lastBuild.details().isBuilding();
		} catch (Exception e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("JenkinsPatchClientImpl.isProdPipelineForPatchRunning.error", new Object[]{patchNumber});
		}
	}

	@Override
	public BuildResult getProdPipelineBuildResult(String patchNumber) {
		JenkinsServer jenkinsServer;
		try {
			jenkinsServer = new JenkinsServer(new URI(jenkinsUrl), jenkinsUser, jenkinsUserAuthKey);
			PipelineBuild lastBuild = getPipelineBuild(jenkinsServer, PATCH_CONS + patchNumber);
			return lastBuild.details().getResult();
		} catch (Exception e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("JenkinsPatchClientImpl.getProdPipelineBuildResult.error", new Object[]{patchNumber});
		}
	}
}
