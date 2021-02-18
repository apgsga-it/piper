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

    private String jenkinsUrl;
    private String jenkinsSshPort;
    private String jenkinsSshUser;
    private JenkinsPipelinePreprocessor preprocessor;
    private OnCloneParameters onCloneParameter;
    private CommandRunner cmdRunner;
    private String jenkinsPipelineRepo;
    private String jenkinsPipelineRepoBranch;

    public static TaskStartOnClonePipeline create() {
        return new TaskStartOnClonePipeline();
    }

    public TaskStartOnClonePipeline jenkinsUrl(String jenkinsUrl) {
        this.jenkinsUrl = jenkinsUrl;
        return this;
    }

    public TaskStartOnClonePipeline jenkinsSshPort(String jenkinsSshPort) {
        this.jenkinsSshPort = jenkinsSshPort;
        return this;
    }

    public TaskStartOnClonePipeline jenkinsSshUser(String jenkinsSshUser) {
        this.jenkinsSshUser = jenkinsSshUser;
        return this;
    }

    public TaskStartOnClonePipeline preprocessor(JenkinsPipelinePreprocessor preprocessor) {
        this.preprocessor = preprocessor;
        return this;
    }

    public TaskStartOnClonePipeline cmdRunner(CommandRunner cmdRunner) {
        this.cmdRunner = cmdRunner;
        return this;
    }

    public TaskStartOnClonePipeline onCloneParameter(OnCloneParameters onCloneParameter) {
        this.onCloneParameter = onCloneParameter;
        return this;
    }

    public TaskStartOnClonePipeline jenkinsPipelineRepo(String jenkinsPipelineRepo) {
        this.jenkinsPipelineRepo = jenkinsPipelineRepo;
        return this;
    }

    public TaskStartOnClonePipeline jenkinsPipelineRepoBranch(String jenkinsPipelineRepoBranch) {
        this.jenkinsPipelineRepoBranch = jenkinsPipelineRepoBranch;
        return this;
    }

    @Override
    public void run() {
        LOGGER.info("Starting onClone Pipeline for following parameter : " + onCloneParameter.toString());
        Map<String,String> parameters = Maps.newHashMap();
        parameters.put("target", onCloneParameter.getTarget() );
        parameters.put("jobPreFix", "onClone");
        parameters.put("parameter", pipelineOnCloneParameterAsJson());
        parameters.put("github_repo", jenkinsPipelineRepo);
        parameters.put("github_repo_branch", jenkinsPipelineRepoBranch);
        // TODO (jhe,che) : Make configurable
        // TODO (jhe, che) : possibly more logging
        parameters.put("script_path", "src/main/groovy/onClonePipeline.groovy");
        JenkinsSshCommand cmd = JenkinsSshCommand.createJenkinsSshBuildJobAndReturnImmediatelyCmd(jenkinsUrl, jenkinsSshPort, jenkinsSshUser, "GenericPipelineJobBuilder", parameters);
        cmdRunner.run(cmd);
    }



    private String pipelineOnCloneParameterAsJson() {
        try {
            List<OnCloneBuildParameters> onCloneBuildParameters = Lists.newArrayList();
            onCloneParameter.getPatchNumbers().forEach(patchNumber -> {
                Patch p = preprocessor.retrievePatch(patchNumber);
                onCloneBuildParameters.add(OnCloneBuildParameters.builder()
                        .patchNumber(patchNumber)
                        .target(onCloneParameter.getTarget())
                        .dbObjectsAsVcsPath(p.getDbPatch().retrieveDbObjectsAsVcsPath())
                        .dbObjects(p.getDbPatch().getDbObjects())
                        .dbPatchTag(p.getDbPatch().getPatchTag())
                        .dbPatchBranch(p.getDbPatch().getDbPatchBranch())
                        .packagers(preprocessor.retrievePackagerInfoFor(Sets.newHashSet(patchNumber), onCloneParameter.getTarget()))
                        .dbZipNames(preprocessor.retrieveDbZipNames(Sets.newHashSet(patchNumber), onCloneParameter.getTarget()))
                        .dockerServices(p.getDockerServices())
                        .services(p.getServices())
                        .build());
            });

            OnCloneAssembleAndDeployParameter adParams = OnCloneAssembleAndDeployParameter.builder()
                    .target(onCloneParameter.getTarget())
                    .packagers(preprocessor.retrievePackagerInfoFor(onCloneParameter.getPatchNumbers(), onCloneParameter.getTarget()))
                    .dbZipNames(preprocessor.retrieveDbZipNames(onCloneParameter.getPatchNumbers(), onCloneParameter.getTarget()))
                    .patchNumbers(onCloneParameter.getPatchNumbers())
                    .build();

            OnClonePipelineParameter pipelineParameter = OnClonePipelineParameter.builder()
                    .target(onCloneParameter.getTarget())
                    .src(onCloneParameter.getSrc())
                    .buildParameters(onCloneBuildParameters)
                    .adParameters(adParams)
                    .build();
            LOGGER.info("OnClonePipelineParameter has been created with following info : " + pipelineParameter.toString());
            ObjectMapper om = new ObjectMapper();
            return om.writeValueAsString(pipelineParameter).replace("\"", "\\\"");
        } catch (Exception e) {
            LOGGER.error("Error while creating OnClonePipelineParameter : " + e.getMessage());
            throw ExceptionFactory.create("Exception : Create Json PARAMETERS for onClone : " + onCloneParameter.toString());
        }
    }
}



