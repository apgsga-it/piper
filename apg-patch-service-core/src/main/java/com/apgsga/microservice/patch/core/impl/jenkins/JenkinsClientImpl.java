package com.apgsga.microservice.patch.core.impl.jenkins;

import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.core.commands.CommandRunner;
import com.apgsga.microservice.patch.core.commands.ProcessBuilderCmdRunnerFactory;
import com.apgsga.microservice.patch.core.commands.jenkins.curl.JenkinsCurlCommand;
import com.apgsga.microservice.patch.core.commands.jenkins.ssh.JenkinsSshCommand;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.google.common.collect.Maps;
import groovy.json.JsonSlurper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JenkinsClientImpl implements JenkinsClient {

	private static final String OK_CONS = "Ok";

	private static final String IS_NOT_BUILDING_CONS = " is not building";

	private static final String CANCEL_CONS = "cancel";

	private static final String JSON_CONS = ".json";

	private static final String PATCH_CONS = "Patch";

	protected static final Log LOGGER = LogFactory.getLog(JenkinsClientImpl.class.getName());

	private Resource dbLocation;
	private String jenkinsUrl;
	private String jenkinsSshPort;
	private String jenkinsSshUser;
	private String jenkinsUserPwd;
	private TaskExecutor threadExecutor;
	private CommandRunner cmdRunner;

	public JenkinsClientImpl(Resource dbLocation, String jenkinsUrl, String jenkinsSshPort, String jenkinsSshUser, String jenkinsUserPwd, TaskExecutor taskExectuor) {
		super();
		this.dbLocation = dbLocation;
		this.jenkinsUrl = jenkinsUrl;
		this.jenkinsSshPort = jenkinsSshPort;
		this.jenkinsSshUser = jenkinsSshUser;
		this.jenkinsUserPwd = jenkinsUserPwd;
		this.threadExecutor = taskExectuor;
		ProcessBuilderCmdRunnerFactory runnerFactory = new ProcessBuilderCmdRunnerFactory();
		cmdRunner = runnerFactory.create();
	}

	@Override
	public void createPatchPipelines(Patch patch) {
		threadExecutor.execute(TaskCreatePatchPipeline.create(jenkinsUrl, jenkinsSshPort, jenkinsSshUser, patch));
	}

	@Override
	public void startProdPatchPipeline(Patch patch) {
		startPipeline(patch, "");

	}

	private void startPipeline(Patch patch, String jobSuffix) {
		try {
			String patchName = PATCH_CONS + patch.getPatchNummer();
			String jobName = patchName + jobSuffix;
			File patchFile = new File(dbLocation.getFile(), patchName + JSON_CONS);
			Map<String,File> fileParams = Maps.newHashMap();
			fileParams.put("patchJson",patchFile);

			if(jobSuffix.equalsIgnoreCase("ondemand")) {
				JenkinsSshCommand onDemandCmd = JenkinsSshCommand.createJenkinsSshBuildJobAndReturnImmediatelyCmd(jenkinsUrl, jenkinsSshPort, jenkinsSshUser, jobName, null, fileParams);
				cmdRunner.run(onDemandCmd);
				LOGGER.info("ondemand job for patch " + patch.getPatchNummer() + " has been started. No post-submit verification will be done.");
			}
			else {
				JenkinsSshCommand buildPipelineCmd = JenkinsSshCommand.createJenkinsSshBuildJobAndWaitForStartCmd(jenkinsUrl, jenkinsSshPort, jenkinsSshUser, jobName, null, fileParams);
				List<String> result = cmdRunner.run(buildPipelineCmd);
				LOGGER.info("Result of Pipeline Job " + jobName + ", : " + result.toString());
			}

		} catch (Exception e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("JenkinsPatchClientImpl.startPipeline.exception",
					new Object[] { e.getMessage(), patch.toString() }, e);
		}
	}

	@Override
	public void processInputAction(Patch patch, Map<String, String> parameter) {
		// TODO (che, 25.4) : Switch to logical Target name
		processInputAction(patch, parameter.get("target"), parameter.get("stage"));
	}

	@Override
	public void processInputAction(Patch patch, String targetName, String stage) {
		String action = stage.equals(CANCEL_CONS) ? stage : PATCH_CONS + patch.getPatchNummer() + stage + targetName + OK_CONS;
		try {
			Map lastBuild = getLastJenkinsJobBuild(PATCH_CONS + patch.getPatchNummer());
			validateLastPipelineBuilder(lastBuild,patch.getPatchNummer());
			inputActionForPipeline(patch, action, PATCH_CONS + patch.getPatchNummer());

		} catch (Exception e) {
			throw ExceptionFactory.createPatchServiceRuntimeException(
					"JenkinsPatchClientImpl.processInputAction.exception",
					new Object[] { e.getMessage(), patch.toString() }, e);
		}
	}
	
	private Map getLastJenkinsJobBuild(String jenkinsJobName) {
		JenkinsCurlCommand lastBuildCmd = JenkinsCurlCommand.createJenkinsCurlGetLastBuildCmd(jenkinsUrl, jenkinsSshUser, jenkinsUserPwd, jenkinsJobName);
		List<String> result = cmdRunner.run(lastBuildCmd);
		JsonSlurper js = new JsonSlurper();
		Map parsedData = (Map) js.parse(new ByteArrayInputStream(result.get(0).getBytes()));
		return (Map) parsedData.get("lastBuild");
	}

	private Map getJenkinsJobsBuilds(String jenkinsJobName) {
		JenkinsCurlCommand lastBuildCmd = JenkinsCurlCommand.createJenkinsCurlGetLastBuildCmd(jenkinsUrl, jenkinsSshUser, jenkinsUserPwd, jenkinsJobName);
		List<String> result = cmdRunner.run(lastBuildCmd);
		JsonSlurper js = new JsonSlurper();
		Map parsedData = (Map) js.parse(new ByteArrayInputStream(result.get(0).getBytes()));
		return parsedData;
	}

	private Map getLastJenkinsPipelineJobInfo(String jenkinsJobName) {
		JenkinsCurlCommand jenkinsCurlCommand = JenkinsCurlCommand.createJenkinsCurlGetJobInputStatus(jenkinsUrl,jenkinsSshUser,jenkinsUserPwd,jenkinsJobName);
		List<String> result = cmdRunner.run(jenkinsCurlCommand);
		JsonSlurper js = new JsonSlurper();
		ArrayList<Map> parsedData = (ArrayList<Map>) js.parse(new ByteArrayInputStream(result.get(0).getBytes()));
		Map lastBuiltState = (Map) parsedData.get(0);
		return lastBuiltState;
	}

	private void inputActionForPipeline(Patch patch, String action, String jobName)
			throws InterruptedException {
		int i = 0; 
		while (waitForAndProcessInput(patch, action, jobName) && i < 10) {
			// Loops with condition
		}
	}

	private boolean waitForAndProcessInput(Patch patch, String action, String jobName) throws InterruptedException {

		// TODO JHE : Define couple of constants ....

		Map lastPipelineBuild = getLastJenkinsJobBuild(jobName);

		// The pipeline never ran
		if(lastPipelineBuild.get("lastBuild") == null) {
			LOGGER.warn("Pipeline for patchNumber: " + patch.getPatchNummer() + IS_NOT_BUILDING_CONS);
			return false;
		}

		// The pipeline is finish
		Map lastBuild = (Map) lastPipelineBuild.get("lastBuild");
		Map lastCompletedBuild = (Map) lastPipelineBuild.get("lastCompletedBuild");
		if(lastCompletedBuild != null && lastBuild.get("number") == lastCompletedBuild.get("number")) {
			LOGGER.warn("Pipeline for patchNumber: " + patch.getPatchNummer() + " " + IS_NOT_BUILDING_CONS);
			return false;
		}

		// If job is not waiting on Input, that means the job is currently running
		Map pipelineJobInfo = getLastJenkinsPipelineJobInfo(jobName);
		if(!((String)pipelineJobInfo.get("status")).equalsIgnoreCase("paused_pending_input")) {
			Thread.sleep(2000);
			return true;
		}
		// We can process the input
		else {
			String lastJobExecutionNumber = (String) getLastJenkinsJobBuild(jobName).get("lastBuild");
			// TODO JHE (01.09.2020) : inputId will have to be build dynamically, or ... really needed to have distinguished name?
			String inputId = "Oktocontinue";
			JenkinsCurlCommand.createJenkinsCurlSubmitPipelineInput(jenkinsUrl,jenkinsSshUser,jenkinsUserPwd,jobName,lastJobExecutionNumber,inputId,action);
			return false;
		}

	}

	private void validateLastPipelineBuilder(Map lastBuildInfo, String patchNumber) throws IOException {
		JsonSlurper js = new JsonSlurper();
		Map lastBuild = (Map) lastBuildInfo.get("lastBuild");
		Map lastSuccessfulBuild = (Map) lastBuildInfo.get("lastSuccessfulBuild");

		// The job never successfuly ran
		if(lastBuild == null || lastSuccessfulBuild == null) {
			LOGGER.warn("Job with patchNumber: " + patchNumber + IS_NOT_BUILDING_CONS);
			return;
		}

		Integer lastBuildNumber = Integer.valueOf(String.valueOf(lastBuild.get("number")));
		Integer lastSuccessfulBuildNumber = Integer.valueOf(String.valueOf(lastSuccessfulBuild.get("number")));

		if (lastBuildNumber == lastSuccessfulBuildNumber) {
			LOGGER.warn("Job with patchNumber: " + patchNumber + IS_NOT_BUILDING_CONS);
		}
	}

	@Override
	public void startAssembleAndDeployPipeline(String target) {
		//TODO JHE (01.09.2020) : Will be started with parameter ... but the pipeline doesn't exist yet ...
		String jobName = "assembleAndDeploy_" + target;
		JenkinsSshCommand cmd = JenkinsSshCommand.createJenkinsSshBuildJobAndReturnImmediatelyCmd(jenkinsUrl, jenkinsSshPort, jenkinsSshUser, jobName);
		cmdRunner.run(cmd);
	}

	@Override
	public void startInstallPipeline(String target) {
		//TODO JHE (01.09.2020) : Will maybe be started with parameter ... but the pipeline doesn't exist yet ...
		String jobName = "install_" + target;
		JenkinsSshCommand cmd = JenkinsSshCommand.createJenkinsSshBuildJobAndReturnImmediatelyCmd(jenkinsUrl, jenkinsSshPort, jenkinsSshUser, jobName);
		cmdRunner.run(cmd);
	}

	@Override
	public void startJenkinsJob(String jobName) {
		JenkinsSshCommand cmd = JenkinsSshCommand.createJenkinsSshBuildJobAndReturnImmediatelyCmd(jenkinsUrl, jenkinsSshPort, jenkinsSshUser, jobName);
		cmdRunner.run(cmd);
	}

	@Override
	public void startJenkinsJob(String jobName, Map<String, String> jobParams) {
		JenkinsSshCommand cmd = JenkinsSshCommand.createJenkinsSshBuildJobAndReturnImmediatelyCmd(jenkinsUrl, jenkinsSshPort, jenkinsSshUser, jobName, jobParams, null);
		cmdRunner.run(cmd);
	}
}