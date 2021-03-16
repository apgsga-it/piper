package com.apgsga.microservice.patch.core.impl.jenkins;

import com.apgsga.microservice.patch.api.*;
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
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

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
		threadExecutor.execute(TaskStartBuildPipeline.create(jenkinsUrl,jenkinsSshPort,jenkinsSshUser,preprocessor,cmdRunner,buildParameters));
	}

	@Override
	public void startAssembleAndDeployPipeline(AssembleAndDeployParameters parameters) {
		AssembleAndDeployPipelineParameter pipelineParameters = AssembleAndDeployPipelineParameter.builder()
					.patchNumbers(parameters.getPatchNumbers())
					.errorNotification(parameters.getErrorNotification())
					.successNotification(parameters.getSuccessNotification())
					.target(parameters.getTarget())
					.packagers(preprocessor.retrievePackagerInfoFor(parameters.getPatchNumbers(),parameters.getTarget()))
					.dbZipNames(preprocessor.retrieveDbZipNames(parameters.getPatchNumbers(),parameters.getTarget()))
				    .dbZipDeployTarget(preprocessor.retrieveDbDeployInstallerHost(parameters.getTarget()))
					.build();
		startGenericPipelineJobBuilder("assembleAndDeploy",
				jenkinsPipelineAssembleScript,
				pipelineParameters.getTarget(),
				formatParameterAsJsonForPipeline(pipelineParameters));
	}

	@Override
	public void startInstallPipeline(InstallParameters parameters) {
		InstallPipelineParameter pipelineParameters = InstallPipelineParameter.builder()
				.target(parameters.getTarget())
				.errorNotification(parameters.getErrorNotification())
				.successNotification(parameters.getSuccessNotification())
				.patchNumbers(parameters.getPatchNumbers())
				.packagers(preprocessor.retrievePackagerInfoFor(parameters.getPatchNumbers(),parameters.getTarget()))
				.installDbPatch(preprocessor.needInstallDbPatchFor(parameters.getPatchNumbers()))
				.dbZipInstallFrom(preprocessor.retrieveDbDeployInstallerHost(parameters.getTarget()))
				.isProductionInstallation(preprocessor.retrieveTargetForStageName("produktion").equalsIgnoreCase(parameters.getTarget()))
				.installDbObjectsInfos(preprocessor.retrieveDbObjectInfoFor(parameters.getPatchNumbers()))
				.build();
		startGenericPipelineJobBuilder("install",
				jenkinsPipelineInstallScript,
				pipelineParameters.getTarget(),
				formatParameterAsJsonForPipeline(pipelineParameters));

	}

	@Override
	public void startOnDemandPipeline(OnDemandParameter parameters) {
		threadExecutor.execute(TaskStartOnDemandPipeline.create(jenkinsUrl,jenkinsSshPort,jenkinsSshUser,preprocessor,cmdRunner,parameters));
	}

	@Override
	public void startOnClonePipeline(OnCloneParameters parameters) {
		TaskStartOnClonePipeline runnable = TaskStartOnClonePipeline.create()
				.jenkinsUrl(jenkinsUrl)
				.jenkinsSshPort(jenkinsSshPort)
				.jenkinsSshUser(jenkinsSshUser)
				.preprocessor(preprocessor)
				.cmdRunner(cmdRunner)
		        .onCloneParameter(parameters)
				.jenkinsPipelineRepo(jenkinsPipelineRepo)
				.jenkinsPipelineRepoBranch(jenkinsPipelineRepoBranch);
		threadExecutor.execute(runnable);
	}

	private String formatParameterAsJsonForPipeline(Object obj) {
		try {
			ObjectMapper om = new ObjectMapper();
			return om.writeValueAsString(obj).replace("\"", "\\\"");
		} catch (JsonProcessingException e) {
			throw ExceptionFactory.create("Exception while trying to format a JSON String for a pipeline parameter");
		}
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