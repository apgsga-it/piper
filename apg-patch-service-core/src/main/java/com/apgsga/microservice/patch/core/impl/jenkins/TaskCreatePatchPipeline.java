package com.apgsga.microservice.patch.core.impl.jenkins;

import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.core.commands.ProcessBuilderCmdRunnerFactory;
import com.apgsga.microservice.patch.core.commands.jenkins.ssh.JenkinsSshCommand;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.google.common.collect.Maps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;

public class TaskCreatePatchPipeline implements Runnable {

	protected static final Log LOGGER = LogFactory.getLog(TaskCreatePatchPipeline.class.getName());

	public static Runnable create(String jenkinsHost, String jenkinsSshPort, String jenkinsSshUser, JenkinsPipelinePreprocessor repo,  Patch patch) {
		return new TaskCreatePatchPipeline(jenkinsHost,jenkinsSshPort,jenkinsSshUser,repo, patch);
	}

	private Patch patch;

	private String jenkinsHost;

	private String jenkinsSshPort;

	private String jenkinsSshUser;

	private JenkinsPipelinePreprocessor preprocessor;

	private TaskCreatePatchPipeline(String jenkinsHost, String jenkinsSshPort, String jenkinsSshUser,JenkinsPipelinePreprocessor preprocessor,  Patch patch) {
		super();
		this.jenkinsHost = jenkinsHost;
		this.jenkinsSshPort = jenkinsSshPort;
		this.jenkinsSshUser = jenkinsSshUser;
		this.preprocessor = preprocessor;
		this.patch = patch;
	}

	@Override
	public void run() {
		try {
			Map<String, String> jobParm = Maps.newHashMap();
			jobParm.put("patchnumber", patch.getPatchNumber());
			jobParm.put("stages", preprocessor.retrieveStagesTargetAsCSV());
			JenkinsSshCommand buildJobCmd = JenkinsSshCommand.createJenkinsSshBuildJobAndWaitForCompleteCmd(jenkinsHost, jenkinsSshPort, jenkinsSshUser, "PatchJobBuilder", jobParm);
			ProcessBuilderCmdRunnerFactory factory = new ProcessBuilderCmdRunnerFactory();
			List<String> result = factory.create().run(buildJobCmd);
			if (!result.stream().anyMatch(c -> {
				return c.contains("SUCCESS");
			})) {
				LOGGER.error("PatchBuilder failed: " + result);
				throw ExceptionFactory.createPatchServiceRuntimeException(
						"JenkinsPatchClientImpl.createPatchPipelines.error",
						new Object[]{patch.toString(), result});
			}
		}
		catch (AssertionError | Exception e) {
			throw ExceptionFactory.createPatchServiceRuntimeException(
					"JenkinsPatchClientImpl.createPatchPipelines.exception",
					new Object[]{e.getMessage(), patch.toString()}, e);
		}


	}


}
