package com.apgsga.microservice.patch.core.impl.jenkins;

import com.apgsga.microservice.patch.api.PatchListParameter;
import com.apgsga.microservice.patch.core.commands.CommandRunner;
import com.apgsga.microservice.patch.core.commands.jenkins.ssh.JenkinsSshCommand;
import com.apgsga.microservice.patch.core.patch.conflicts.PatchConflict;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskStartNotificationForPatchConflictPipeline implements Runnable {

    protected static final Log LOGGER = LogFactory.getLog(TaskStartNotificationForPatchConflictPipeline.class.getName());

    private final String NOTIFICATION_FOR_CONFLICT_PIPELINE_NAME = "patchConflictNotificationPipeline";

    private String jenkinsUrl;
    private String jenkinsSshPort;
    private String jenkinsSshUser;
    private List<PatchListParameter> patchListParameters;
    private List<PatchConflict> patchConflictParameters;
    private CommandRunner cmdRunner;


    private TaskStartNotificationForPatchConflictPipeline(String jenkinsUrl, String jenkinsSshPort, String jenkinsSshUser, List<PatchListParameter> patchListParameters, List<PatchConflict> patchConflicts, CommandRunner cmdRunner) {
        this.jenkinsUrl = jenkinsUrl;
        this.jenkinsSshPort = jenkinsSshPort;
        this.jenkinsSshUser = jenkinsSshUser;
        this.patchListParameters = patchListParameters;
        this.patchConflictParameters = patchConflicts;
        this.cmdRunner = cmdRunner;
    }

    public static TaskStartNotificationForPatchConflictPipeline create(String jenkinsUrl, String jenkinsSshPort, String jenkinsSshUser, List<PatchListParameter> patchListParameters, List<PatchConflict> patchConflicts, CommandRunner cmdRunner) {
        return new TaskStartNotificationForPatchConflictPipeline(jenkinsUrl,jenkinsSshPort,jenkinsSshUser, patchListParameters, patchConflicts,cmdRunner);
    }

    @Override
    public void run() {
        String patches = patchListParameters.stream().map(PatchListParameter::getPatchNumber).collect(Collectors.joining(","));
        LOGGER.info("TaskStartNotificationForPatchConflictPipeline running for following patch: " + patches);
        Map<String,String> jobParameters = Maps.newHashMap();
        jobParameters.put("PARAMETERS", notificationForPatchConflictParameterAsJson());
        LOGGER.info("TaskStartNotificationForPatchConflictPipeline Parameters passed to " + NOTIFICATION_FOR_CONFLICT_PIPELINE_NAME + " Pipeline :" + jobParameters.toString());
        JenkinsSshCommand.createJenkinsSshBuildJobAndReturnImmediatelyCmd(jenkinsUrl, jenkinsSshPort, jenkinsSshUser, NOTIFICATION_FOR_CONFLICT_PIPELINE_NAME, jobParameters);
        LOGGER.info(NOTIFICATION_FOR_CONFLICT_PIPELINE_NAME + " pipeline has been started, Piper didn't wait for a confirmation that the Job successfully started.");

    }

    private String notificationForPatchConflictParameterAsJson() {
        try {
            List<NotificationForPatchConflictPipelineParameters> params = Lists.newArrayList();

            patchConflictParameters.forEach(pc -> {
                NotificationForPatchConflictPipelineParameters.builder()
                        .patchConflict(pc)
                        .emailAdress(emailAdressFor(pc))
                        .build();
            });


            ObjectMapper om = new ObjectMapper();
            return om.writeValueAsString(params).replace("\"","\\\"");
        } catch (JsonProcessingException e) {
            LOGGER.error("Error while creating notificationForPatchConflictParameterAsJson : " + e.getMessage());
            throw ExceptionFactory.create("Exception : Create Json PARAMETERS for " + NOTIFICATION_FOR_CONFLICT_PIPELINE_NAME + " pipeline.");
        }
    }

    private List<String> emailAdressFor(PatchConflict pc) {
        List<String> result = Lists.newArrayList();
        patchListParameters.forEach(plp -> {
            //TODO JHE : to be verified, but most probably this will add duplicate adresses, we might want a Set instead of List
            if(plp.getPatchNumber().equals(pc.getP1().getPatchNumber()) || plp.getPatchNumber().equals(pc.getP2().getPatchNumber())) {
                result.addAll(plp.getEmails());
            }
        });
        return result;
    }
}
