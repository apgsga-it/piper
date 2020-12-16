package com.apgsga.microservice.patch.core.impl.jenkins;

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
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.util.List;
import java.util.Map;

public class TaskStartBuildPipeline implements Runnable {

    protected static final Log LOGGER = LogFactory.getLog(TaskStartBuildPipeline.class.getName());

    private static final String PATCH_CONS = "Patch";

    private final String jenkinsUrl;
    private final String jenkinsSshPort;
    private final String jenkinsSshUser;
    private final JenkinsPipelinePreprocessor preprocessor;
    private final BuildParameter buildParameters;
    private final String dbLocation;
    private final CommandRunner cmdRunner;

    public static Runnable create(String jenkinsUrl, String jenkinsSshPort, String jenkinsSshUser, JenkinsPipelinePreprocessor preprocessor, String dbLocation, CommandRunner cmdRunner, BuildParameter buildParameters) {
        return new TaskStartBuildPipeline(jenkinsUrl,jenkinsSshPort,jenkinsSshUser,preprocessor,dbLocation,cmdRunner,buildParameters);
    }

    private TaskStartBuildPipeline(String jenkinsUrl, String jenkinsSshPort, String jenkinsSshUser, JenkinsPipelinePreprocessor preprocessor, String dbLocation,CommandRunner cmdRunner, BuildParameter buildParameters) {
        super();
        this.jenkinsUrl = jenkinsUrl;
        this.jenkinsSshPort = jenkinsSshPort;
        this.jenkinsSshUser = jenkinsSshUser;
        this.preprocessor = preprocessor;
        this.buildParameters = buildParameters;
        this.dbLocation = dbLocation;
        this.cmdRunner = cmdRunner;
    }

    private String pipelineBuildParameterAsJson() {
        try {
            Patch patch = preprocessor.retrievePatch(buildParameters.getPatchNumber());
            PipelineBuildParameter pipelineBuildParameter = PipelineBuildParameter.builder()
                    .patchNumber(buildParameters.getPatchNumber())
                    .stageName(buildParameters.getStageName())
                    .successNotification(buildParameters.getSuccessNotification())
                    .errorNotification(buildParameters.getErrorNotification())
                    .patchTag(patch.getPatchTag())
                    .dbObjectAsVcsPath(patch.retrieveDbObjectsAsVcsPath())
                    .dbObjects(patch.getDbObjects())
                    .dbPatchBranch(patch.getDbPatchBranch())
                    .dockerServices(patch.getDockerServices())
                    .services(patch.getServices())
                    .target(preprocessor.retrieveTargetForStageName(buildParameters.getStageName()))
                    .build();
            LOGGER.info("PipelineBuildParameter has been created with following info : " + pipelineBuildParameter.toString());
            ObjectMapper om = new ObjectMapper();
            return om.writeValueAsString(pipelineBuildParameter).replace("\"","\\\"");
        } catch (JsonProcessingException e) {
            throw ExceptionFactory.create("Exception : Create Json PARAMETERS for Patch <%s> and Stage <%s>",buildParameters.getPatchNumber(),buildParameters.getStageName());
        }
    }

    @Override
    public void run() {
            LOGGER.info("TaskStartBuildPipeline running for following buildParameter: " + buildParameters.toString());
            preprocessor.preProcessBuildPipeline(buildParameters);
            String patchName = PATCH_CONS + buildParameters.getPatchNumber();
            String jobName = patchName + "_build_" + buildParameters.getStageName();
            final ResourceLoader rl = new FileSystemResourceLoader();
            Map<String,String> jobParameters = Maps.newHashMap();
            jobParameters.put("PARAMETERS",pipelineBuildParameterAsJson());
            LOGGER.info("JobParameters passed to " + jobName + " Pipeline :" + jobParameters.toString());
            JenkinsSshCommand buildPipelineCmd = JenkinsSshCommand.createJenkinsSshBuildJobAndWaitForStartCmd(jenkinsUrl, jenkinsSshPort, jenkinsSshUser, jobName, jobParameters);
            List<String> result = cmdRunner.run(buildPipelineCmd);
            LOGGER.info("Result of Pipeline Job " + jobName + ", : " + result.toString());
    }
}
