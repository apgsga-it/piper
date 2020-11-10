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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Profile("live")
@Component("jenkinsBean")
public class JenkinsClientImpl implements JenkinsClient {

	private static final String OK_CONS = "Ok";

	private static final String IS_NOT_BUILDING_CONS = " is not building";

	private static final String CANCEL_CONS = "cancel";

	private static final String JSON_CONS = ".json";

	private static final String PATCH_CONS = "Patch";

	protected static final Log LOGGER = LogFactory.getLog(JenkinsClientImpl.class.getName());

	@Value("${json.db.location:db}")
	private String dbLocation;

	@Value("${jenkins.host:jenkins.apgsga.ch}")
	public String jenkinsUrl;

	@Value("${jenkins.port:8080}")
	public String jenkinsPort;

	@Value("${jenkins.ssh.port:53801}")
	public String jenkinsSshPort;

	@Value("${jenkins.ssh.user:apg_install}")
	public String jenkinsSshUser;

	@Value("${jenkins.authkey}")
	public String jenkinsUserPwd;

	@Value("${jenkins.pipeline.repo}")
	public String jenkinsPipelineRepo;

	@Value("${jenkins.pipeline.repo.branch}")
	public String jenkinsPipelineRepoBranch;

	@Value("${jenkins.pipeline.repo.install.script}")
	public String jenkinsPipelineInstallScript;

	@Value("${jenkins.pipeline.repo.assemble.script}")
	public String jenkinsPipelineAssembleScript;

	@Autowired
	private TaskExecutor threadExecutor;
	private CommandRunner cmdRunner;

	public JenkinsClientImpl() {
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
			final ResourceLoader rl = new FileSystemResourceLoader();
			Resource rDbLocation = rl.getResource(dbLocation);
			File patchFile = new File(rDbLocation.getFile(), patchName + JSON_CONS);
			Map<String,String> fileParams = Maps.newHashMap();
			fileParams.put("patchFile.json",patchFile.getAbsolutePath());

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
		String target = parameter.get("target");
		String stage = parameter.get("stage");
		LOGGER.info("Processing input action for patch " + patch.getPatchNummer() + " with target=" + target + " and stage=" + stage);
		processInputAction(patch, target, stage);
	}

	@Override
	public void processInputAction(Patch patch, String targetName, String stage) {
		String jobInputId = stage.equals(CANCEL_CONS) ? stage : PATCH_CONS + patch.getPatchNummer() + stage + targetName + OK_CONS;
		try {
			Map lastBuild = getJenkinsJobBuild(PATCH_CONS + patch.getPatchNummer());
			validateLastPipelineBuilder(lastBuild,patch.getPatchNummer());
			inputActionForPipeline(patch, jobInputId, PATCH_CONS + patch.getPatchNummer());

		} catch (Exception e) {
			throw ExceptionFactory.createPatchServiceRuntimeException(
					"JenkinsPatchClientImpl.processInputAction.exception",
					new Object[] { e.getMessage(), patch.toString() }, e);
		}
	}
	
	private Map getJenkinsJobBuild(String jenkinsJobName) {
		String jenkinsUrlAndPort = jenkinsUrl + ":" + jenkinsPort;
		JenkinsCurlCommand lastBuildCmd = JenkinsCurlCommand.createJenkinsCurlGetLastBuildCmd(jenkinsUrlAndPort, jenkinsSshUser, jenkinsUserPwd, jenkinsJobName);
		LOGGER.info("Getting last jenkins job build with following parameter: jenkinsUrl=" + jenkinsUrlAndPort
				+ ", jenkinsSshUser=" + jenkinsSshUser
				+ ", jenkinsUserPwd=" + jenkinsUserPwd
				+ ", jenkinsJobName=" + jenkinsJobName);
		List<String> result = cmdRunner.run(lastBuildCmd);
		JsonSlurper js = new JsonSlurper();
		Map parsedData = (Map) js.parse(new ByteArrayInputStream(result.get(0).getBytes()));
		return parsedData;
	}


	private Map getLastJenkinsPipelineJobInfo(String jenkinsJobName) {
		String jenkinsUrlAndPort = jenkinsUrl + ":" + jenkinsPort;
		JenkinsCurlCommand jenkinsCurlCommand = JenkinsCurlCommand.createJenkinsCurlGetJobInputStatus(jenkinsUrlAndPort,jenkinsSshUser,jenkinsUserPwd,jenkinsJobName);
		List<String> result = cmdRunner.run(jenkinsCurlCommand);
		JsonSlurper js = new JsonSlurper();
		ArrayList<Map> parsedData = (ArrayList<Map>) js.parse(new ByteArrayInputStream(result.get(0).getBytes()));
		Map lastBuiltState = (Map) parsedData.get(0);
		return lastBuiltState;
	}

	private void inputActionForPipeline(Patch patch, String inputId, String jobName)
			throws InterruptedException {
		int i = 0; 
		while (waitForAndProcessInput(patch, inputId, jobName) && i < 10) {
			// Loops with condition
		}
	}

	private boolean waitForAndProcessInput(Patch patch, String inputId, String jobName) throws InterruptedException {

		// TODO JHE : Define couple of constants ....

		Map lastPipelineBuild = getJenkinsJobBuild(jobName);

		// The pipeline never ran or didn't start yet
		if(lastPipelineBuild.get("lastBuild") == null) {
			LOGGER.warn("Pipeline for patchNumber: " + patch.getPatchNummer() + IS_NOT_BUILDING_CONS);
			return false;
		}

		// The pipeline is finish
		Map lastBuild = (Map) lastPipelineBuild.get("lastBuild");
		Map lastCompletedBuild = (Map) lastPipelineBuild.get("lastCompletedBuild");
		if(lastCompletedBuild != null && lastBuild.get("number") == lastCompletedBuild.get("number")) {
			LOGGER.warn("Pipeline for patchNumber: " + patch.getPatchNummer() + " is finish.");
			return false;
		}

		// If job is not waiting on Input, that means the job is currently running
		Map pipelineJobInfo = getLastJenkinsPipelineJobInfo(jobName);
		if(!((String)pipelineJobInfo.get("status")).equalsIgnoreCase("paused_pending_input")) {
			LOGGER.info("Pipeline for patchNumber " + patch.getPatchNummer() + " currently running, waiting 2 seconds ...");
			Thread.sleep(2000);
			return true;
		}
		// We can process the input
		else {
			LOGGER.info("Pipeline for patchNumber " + patch.getPatchNummer() + " is waiting on Input, process can continue...");
			Map lastJob = (Map) getJenkinsJobBuild(jobName).get("lastBuild");
			String lastJobExecutionNumber = String.valueOf((Integer)lastJob.get("number"));
			JenkinsCurlCommand submitPipelineInputCmd = JenkinsCurlCommand.createJenkinsCurlSubmitPipelineInput(jenkinsUrl + ":" + jenkinsPort, jenkinsSshUser, jenkinsUserPwd, jobName, lastJobExecutionNumber,inputId,"proceedEmpty");
			cmdRunner.run(submitPipelineInputCmd);
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
	public void startAssembleAndDeployPipeline(String target, String parameter) {
		startGenericPipelineJobBuilder("assembleAndDeploy", target, parameter);
	}

	@Override
	public void startInstallPipeline(String target, String parameter) {
		startGenericPipelineJobBuilder("install", target, parameter);

	}

	private void startGenericPipelineJobBuilder(String jobPreFix, String target, String parameter) {
		Map<String,String> parameters = Maps.newHashMap();
		parameters.put("target", target );
		parameters.put("jobPreFix", jobPreFix);
		parameters.put("parameter", parameter);
		parameters.put("github_repo", jenkinsPipelineRepo);
		parameters.put("github_repo_branch", jenkinsPipelineRepoBranch);
		parameters.put("script_path", this.jenkinsPipelineAssembleScript);
		JenkinsSshCommand cmd = JenkinsSshCommand.createJenkinsSshBuildJobAndReturnImmediatelyCmd(jenkinsUrl, jenkinsSshPort, jenkinsSshUser, "GenericPipelineJobBuilder", parameters);
		cmdRunner.run(cmd);
	}

}