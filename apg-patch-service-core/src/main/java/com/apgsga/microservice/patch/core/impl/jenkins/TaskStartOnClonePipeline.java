package com.apgsga.microservice.patch.core.impl.jenkins;

import com.apgsga.microservice.patch.api.OnCloneParameters;
import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.core.commands.CommandRunner;
import com.apgsga.microservice.patch.core.commands.jenkins.ssh.JenkinsSshCommand;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;

public class TaskStartOnClonePipeline implements Runnable {

    protected static final Log LOGGER = LogFactory.getLog(TaskStartOnClonePipeline.class.getName());

    private final String ON_CLONE_JOB_PREFIX = "onClone";

    private final String jenkinsUrl;
    private final String jenkinsSshPort;
    private final String jenkinsSshUser;
    private final JenkinsPipelinePreprocessor preprocessor;
    private final OnCloneParameters onCloneParameter;
    private final CommandRunner cmdRunner;

    public TaskStartOnClonePipeline(String jenkinsUrl, String jenkinsSshPort, String jenkinsSshUser, JenkinsPipelinePreprocessor preprocessor, CommandRunner cmdRunner, OnCloneParameters onCloneParameter) {
        super();
        this.jenkinsUrl = jenkinsUrl;
        this.jenkinsSshPort = jenkinsSshPort;
        this.jenkinsSshUser = jenkinsSshUser;
        this.preprocessor = preprocessor;
        this.cmdRunner = cmdRunner;
        this.onCloneParameter = onCloneParameter;
    }

    public static Runnable create(String jenkinsUrl, String jenkinsSshPort, String jenkinsSshUser, JenkinsPipelinePreprocessor preprocessor, CommandRunner cmdRunner, OnCloneParameters onCloneParameter) {
        return new TaskStartOnClonePipeline(jenkinsUrl,jenkinsSshPort,jenkinsSshUser,preprocessor,cmdRunner,onCloneParameter);
    }

    @Override
    public void run() {
        LOGGER.info("Starting onClone Pipeline for following parameter : " + onCloneParameter.toString());
        String jobName = ON_CLONE_JOB_PREFIX + onCloneParameter.getTarget();
        Map<String,String> jobParameters = Maps.newHashMap();
        jobParameters.put("PARAMETERS",pipelineOnCloneParameterAsJson());
        LOGGER.info("OnClone Parameters passed to " + jobName + " Pipeline :" + jobParameters.toString());
        JenkinsSshCommand buildPipelineCmd = JenkinsSshCommand.createJenkinsSshBuildJobAndWaitForStartCmd(jenkinsUrl, jenkinsSshPort, jenkinsSshUser, jobName, jobParameters);
        List<String> result = cmdRunner.run(buildPipelineCmd);
        LOGGER.info("Result of OnClone Pipeline Job " + jobName + ", : " + result.toString());
    }

    private String pipelineOnCloneParameterAsJson() {
        try {
            List<OnClonePatchParameters> onClonePatchParameters = Lists.newArrayList();
            onCloneParameter.getPatchNumbers().forEach(patchNumber -> {
                Patch p = preprocessor.retrievePatch(patchNumber);
                onClonePatchParameters.add(OnClonePatchParameters.builder().patchNumber(patchNumber)
                                                                           .dbObjectsAsVcsPath(p.getDbPatch().retrieveDbObjectsAsVcsPath())
                                                                           .dbObjects(p.getDbPatch().getDbObjects())
                                                                           .dbPatchTag(p.getDbPatch().getPatchTag())
                                                                           .dbPatchBranch(p.getDbPatch().getDbPatchBranch())
                                                                           .packagers(preprocessor.retrievePackagerInfoFor(Sets.newHashSet(patchNumber),onCloneParameter.getTarget()))
                                                                           .dbZipNames(preprocessor.retrieveDbZipNames(Sets.newHashSet(patchNumber),onCloneParameter.getTarget()))
                                                                           .dockerServices(p.getDockerServices())
                                                                           .services(p.getServices()).build());
            });

            OnClonePipelineParameter pipelineParameter = OnClonePipelineParameter.builder().target(onCloneParameter.getTarget())
                                                                                           .src(onCloneParameter.getSrc())
                                                                                           .patches(onClonePatchParameters)
                                                                                           .build();
            LOGGER.info("OnClonePipelineParameter has been created with following info : " + pipelineParameter.toString());
            ObjectMapper om = new ObjectMapper();
            return om.writeValueAsString(pipelineParameter).replace("\"","\\\"");
        } catch (Exception e) {
            LOGGER.error("Error while creating OnClonePipelineParameter : " + e.getMessage());
            throw ExceptionFactory.create("Exception : Create Json PARAMETERS for onClone : " + onCloneParameter.toString());
        }
    }
}
