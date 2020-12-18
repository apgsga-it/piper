package com.apgsga.microservice.patch.core.impl.jenkins;

import com.apgsga.microservice.patch.api.AssembleAndDeployParameters;
import com.apgsga.microservice.patch.api.BuildParameter;
import com.apgsga.microservice.patch.api.InstallParameters;
import com.apgsga.microservice.patch.api.Package;
import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.core.commands.CommandRunner;
import com.apgsga.microservice.patch.core.commands.jenkins.ssh.JenkinsSshCommand;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
		threadExecutor.execute(TaskStartBuildPipeline.create(jenkinsUrl,jenkinsSshPort,jenkinsSshUser,preprocessor,dbLocation,cmdRunner,buildParameters));
	}

	@Override
	public void startAssembleAndDeployPipeline(AssembleAndDeployParameters parameters) {
		AssembleAndDeployPipelineParameter pipelineParameters = AssembleAndDeployPipelineParameter.builder()
					.patchNumbers(parameters.getPatchNumbers())
					.errorNotification(parameters.getErrorNotification())
					.successNotification(parameters.getSuccessNotification())
					.target(parameters.getTarget())
					.packagers(retrievePackagerInfoForAssembleAndDeploy(parameters.getPatchNumbers(),parameters.getTarget()))
					.dbZipNames(preprocessor.retrieveDbZipNames(parameters.getPatchNumbers(),parameters.getTarget()))
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
				.packagers(retrievePackagerInfoForInstall(parameters.getPatchNumbers(),parameters.getTarget()))
				.build();
		startGenericPipelineJobBuilder("install",
				jenkinsPipelineInstallScript,
				pipelineParameters.getTarget(),
				formatParameterAsJsonForPipeline(pipelineParameters));

	}

	private List<InstallPipelineParameter.PackagerInfo> retrievePackagerInfoForInstall(Set<String> patchNumbers, String target) {
		List<InstallPipelineParameter.PackagerInfo> packagers = Lists.newArrayList();
		patchNumbers.forEach(number -> {
			preprocessor.retrievePatch(number).getServices().forEach(service -> {
				preprocessor.packagesFor(service).forEach(aPackage -> {
					if(!packagers.stream().anyMatch(p -> p.name.equals(aPackage.getPackagerName()))) {
						packagers.add(new InstallPipelineParameter.PackagerInfo(aPackage.getPackagerName()
							,preprocessor.retrieveTargetHostFor(service,target)
							,preprocessor.retrieveBaseVersionFor(service)
							,preprocessor.retrieveVcsBranchFor(service)));
					}
				});
			});
		});
		return packagers;
	}

	private String formatParameterAsJsonForPipeline(Object obj) {
		try {
			ObjectMapper om = new ObjectMapper();
			return om.writeValueAsString(obj).replace("\"", "\\\"");
		} catch (JsonProcessingException e) {
			throw ExceptionFactory.create("Exception while trying to format a JSON String for a pipeline parameter");
		}
	}

	private List<AssembleAndDeployPipelineParameter.PackagerInfo> retrievePackagerInfoForAssembleAndDeploy(Set<String> patchNumbers, String target) {
		List<AssembleAndDeployPipelineParameter.PackagerInfo> packagers = Lists.newArrayList();
		patchNumbers.forEach(number -> {
			preprocessor.retrievePatch(number).getServices().forEach(service -> {
				preprocessor.packagesFor(service).forEach(aPackage -> {
					if(!packagers.stream().anyMatch(p -> p.name.equals(aPackage.getPackagerName()))) {
						packagers.add(new AssembleAndDeployPipelineParameter.PackagerInfo(aPackage.getPackagerName()
								,preprocessor.retrieveTargetHostFor(service, target)
								,preprocessor.retrieveBaseVersionFor(service)
								,preprocessor.retrieveVcsBranchFor(service)));
					}
				});
			});
		});
		return packagers;
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