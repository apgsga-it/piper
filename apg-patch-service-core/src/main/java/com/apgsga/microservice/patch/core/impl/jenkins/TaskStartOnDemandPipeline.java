package com.apgsga.microservice.patch.core.impl.jenkins;

import com.apgsga.microservice.patch.api.OnDemandParameter;
import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.Service;
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
import java.util.stream.Collectors;

public class TaskStartOnDemandPipeline implements Runnable {

    protected static final Log LOGGER = LogFactory.getLog(TaskStartOnDemandPipeline.class.getName());

    private static final String ON_DEMAND_PREFIX = "Patch";

    private static final String ON_DEMAND_SUFFIX = "OnDemand";

    private final String jenkinsUrl;
    private final String jenkinsSshPort;
    private final String jenkinsSshUser;
    private final JenkinsPipelinePreprocessor preprocessor;
    private final OnDemandParameter onDemandParameter;
    private final CommandRunner cmdRunner;
    private final String dockerInstallScriptPath;

    public TaskStartOnDemandPipeline(String jenkinsUrl, String jenkinsSshPort, String jenkinsSshUser, JenkinsPipelinePreprocessor preprocessor, CommandRunner cmdRunner, OnDemandParameter onDemandParameter, String dockerInstallScriptPath) {
        super();
        this.jenkinsUrl = jenkinsUrl;
        this.jenkinsSshPort = jenkinsSshPort;
        this.jenkinsSshUser = jenkinsSshUser;
        this.preprocessor = preprocessor;
        this.cmdRunner = cmdRunner;
        this.onDemandParameter = onDemandParameter;
        this.dockerInstallScriptPath = dockerInstallScriptPath;
    }

    public static Runnable create(String jenkinsUrl, String jenkinsSshPort, String jenkinsSshUser, JenkinsPipelinePreprocessor preprocessor, CommandRunner cmdRunner, OnDemandParameter onDemandParameter, String dockerInstallScriptPath) {
        return new TaskStartOnDemandPipeline(jenkinsUrl,jenkinsSshPort,jenkinsSshUser,preprocessor,cmdRunner,onDemandParameter,dockerInstallScriptPath);
    }

    @Override
    public void run() {
        LOGGER.info("TaskStartOnDemandPipeline running for following buildParameter: " + onDemandParameter.toString());
        String jobName = ON_DEMAND_PREFIX + onDemandParameter.getPatchNumber() + ON_DEMAND_SUFFIX;
        Map<String,String> jobParameters = Maps.newHashMap();
        jobParameters.put("PARAMETERS",pipelineOnDemandParameterAsJson());
        LOGGER.info("OnDemand Parameters passed to " + jobName + " Pipeline :" + jobParameters.toString());
        JenkinsSshCommand buildPipelineCmd = JenkinsSshCommand.createJenkinsSshBuildJobAndWaitForStartCmd(jenkinsUrl, jenkinsSshPort, jenkinsSshUser, jobName, jobParameters);
        List<String> result = cmdRunner.run(buildPipelineCmd);
        LOGGER.info("Result of onDemand Pipeline Job " + jobName + ", : " + result.toString());
    }

    private boolean needToDealWithDb() {
        return preprocessor.isTargetPartOfStageMapping(onDemandParameter.getTarget()) || preprocessor.isDbConfiguredFor(onDemandParameter.getTarget());
    }

    private String pipelineOnDemandParameterAsJson() {
        try {
            Patch patch = preprocessor.retrievePatch(onDemandParameter.getPatchNumber());
            OnDemandPipelineParameter onDemandPipelineParameter = OnDemandPipelineParameter.builder()
                    .patchNumber(onDemandParameter.getPatchNumber())
                    .target(onDemandParameter.getTarget())
                    .developerBranch(patch.getDeveloperBranch())
                    .dbObjectsAsVcsPath(needToDealWithDb() ? patch.getDbPatch().retrieveDbObjectsAsVcsPath() : Lists.newArrayList())
                    .dbPatchTag(patch.getDbPatch().getPatchTag())
                    .dbObjects(needToDealWithDb() ? patch.getDbPatch().getDbObjects() : Lists.newArrayList())
                    .dbPatchBranch(patch.getDbPatch().getDbPatchBranch())
                    .dockerServices(patch.getDockerServices())
                    .services(preprocessor.reduceOnlyServicesConfiguredForTarget(patch.getServices(),onDemandParameter.getTarget()))
                    .artifactsToBuild(patch.getServices().stream().collect(Collectors.toMap(Service::getServiceName, Service::retrieveMavenArtifactsToBuild)))
                    .packagers(preprocessor.retrievePackagerInfoFor(Sets.newHashSet(onDemandParameter.getPatchNumber()),onDemandParameter.getTarget()))
                    .dbZipNames(preprocessor.retrieveDbZipNames(Sets.newHashSet(onDemandParameter.getPatchNumber()),onDemandParameter.getTarget()))
                    .dbZipDeployTarget(preprocessor.retrieveDbDeployInstallerHost(onDemandParameter.getTarget()))
                    .installDbPatch(preprocessor.needInstallDbPatchFor(Sets.newHashSet(onDemandParameter.getPatchNumber()),onDemandParameter.getTarget()))
                    .dbZipInstallFrom(preprocessor.retrieveDbDeployInstallerHost(onDemandParameter.getTarget()))
                    .installDockerServices(preprocessor.needInstallDockerServicesFor(Sets.newHashSet(onDemandParameter.getPatchNumber())))
                    .pathToDockerInstallScript(dockerInstallScriptPath)
                    .build();
            LOGGER.info("onDemandPipelineParameter has been created with following info : " + onDemandPipelineParameter.toString());
            ObjectMapper om = new ObjectMapper();
            return om.writeValueAsString(onDemandPipelineParameter).replace("\"","\\\"");
        } catch (Exception e) {
            LOGGER.error("Error while creating onDemandPipelineParameter : " + e.getMessage());
            throw ExceptionFactory.create("Exception : Create Json PARAMETERS for Patch <%s>",onDemandParameter.getPatchNumber());
        }
    }
}
