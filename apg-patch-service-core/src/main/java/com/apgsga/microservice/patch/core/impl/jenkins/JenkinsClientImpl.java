package com.apgsga.microservice.patch.core.impl.jenkins;

import com.apgsga.microservice.patch.api.AssembleAndDeployParameters;
import com.apgsga.microservice.patch.api.BuildParameter;
import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.core.commands.CommandRunner;
import com.apgsga.microservice.patch.core.commands.jenkins.ssh.JenkinsSshCommand;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
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

import java.io.File;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@Profile("live")
@Component("jenkinsBean")
public class JenkinsClientImpl implements JenkinsClient {

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

	@Autowired
	private JenkinsPipelinePreprocessor preprocessor;

	@Autowired
	private CommandRunner cmdRunner;


	@Override
	public void createPatchPipelines(Patch patch) {
		threadExecutor.execute(TaskCreatePatchPipeline.create(jenkinsUrl, jenkinsSshPort, jenkinsSshUser, preprocessor, patch));
	}

	@Override
	public void startProdBuildPatchPipeline(BuildParameter buildParameters) {
		startBuildPipeline(buildParameters);
	}

	// TODO (che,jhe 9.12.20) : This should be probably also in a background Thread
	private void startBuildPipeline(BuildParameter bp) {
		try {
			preprocessor.preProcessBuildPipeline(bp);
			String patchName = PATCH_CONS + bp.getPatchNumber();
			String jobName = patchName + "_build_" + bp.getStageName();
			final ResourceLoader rl = new FileSystemResourceLoader();
			Resource rDbLocation = rl.getResource(dbLocation);
			File patchFile = new File(rDbLocation.getFile(), patchName + JSON_CONS);
			Map<String,String> fileParams = Maps.newHashMap();
			fileParams.put("patchFile.json",patchFile.getAbsolutePath());
			Map<String,String> jobParameters = Maps.newHashMap();
			jobParameters.put("TARGET",preprocessor.retrieveTargetForStageName(bp.getStageName()));
			jobParameters.put("STAGE",bp.getStageName());
			jobParameters.put("SUCCESS_NOTIFICATION",bp.getSuccessNotification());
			jobParameters.put("ERROR_NOTIFICATION", bp.getErrorNotification());
			LOGGER.info("JobParameters passed to " + jobName + " Pipeline :" + jobParameters.toString());
			JenkinsSshCommand buildPipelineCmd = JenkinsSshCommand.createJenkinsSshBuildJobAndWaitForStartCmd(jenkinsUrl, jenkinsSshPort, jenkinsSshUser, jobName, jobParameters, fileParams);
			List<String> result = cmdRunner.run(buildPipelineCmd);
			LOGGER.info("Result of Pipeline Job " + jobName + ", : " + result.toString());
		} catch (Exception e) {
			throw ExceptionFactory.create("Exception: <%s> while starting the Jenkins Build Pipeline Job for Patch:  %s ",e,
					 e.getMessage(), bp.getPatchNumber() );
		}
	}

	@Override
	public void startAssembleAndDeployPipeline(AssembleAndDeployParameters parameters) {
		ObjectMapper om = new ObjectMapper();
		try {
			startGenericPipelineJobBuilder("assembleAndDeploy", jenkinsPipelineAssembleScript, parameters.getTarget(), om.writeValueAsString(parameters));
		} catch (JsonProcessingException e) {
			throw ExceptionFactory.create("Exception: <%s> while starting the Jenkins Assemble and Deploy Pipeline Job for Patch:  %s ", e,
					e.getMessage(),parameters.toString());
		}
	}

	@Override
	public void startInstallPipeline(String target, String parameter) {
		startGenericPipelineJobBuilder("install", jenkinsPipelineInstallScript, target, parameter);

	}

	private void startGenericPipelineJobBuilder(String jobPreFix, String scriptPath, String target, String parameter) {
		Map<String,String> parameters = Maps.newHashMap();
		parameters.put("target", target );
		parameters.put("jobPreFix", jobPreFix);
		parameters.put("parameter", parameter);
		parameters.put("github_repo", jenkinsPipelineRepo);
		parameters.put("github_repo_branch", jenkinsPipelineRepoBranch);
		parameters.put("script_path", scriptPath);
		JenkinsSshCommand cmd = JenkinsSshCommand.createJenkinsSshBuildJobAndReturnImmediatelyCmd(jenkinsUrl, jenkinsSshPort, jenkinsSshUser, "GenericPipelineJobBuilder", parameters);
		cmdRunner.run(cmd);
	}


}