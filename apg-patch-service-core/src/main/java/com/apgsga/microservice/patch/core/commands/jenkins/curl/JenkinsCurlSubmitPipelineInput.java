package com.apgsga.microservice.patch.core.commands.jenkins.curl;

import com.google.common.collect.Lists;

import java.util.List;

public class JenkinsCurlSubmitPipelineInput extends JenkinsCurlCommand {

    private String jobName;

    private String jobExecutionNumber;

    private String action;

    private String inputId;

    public JenkinsCurlSubmitPipelineInput(String jenkinsUrl, String jenkinsUserName, String jenkinsUserPwd, String jobName, String jobExecutionNumber, String inputId, String action) {
        super(jenkinsUrl,jenkinsUserName,jenkinsUserPwd);
        this.jobName = jobName;
        this.jobExecutionNumber = jobExecutionNumber;
        this.action = action;
        this.inputId = inputId;
    }

    @Override
    protected String[] getCurlCmd() {
        List<String> cmd = Lists.newArrayList();
        cmd.add("-X");
        cmd.add("POST");
        cmd.add(JENKINS_URL + "/job/" + jobName + "/" + jobExecutionNumber + "/input/" + inputId + "/" + action);
        return cmd.stream().toArray(String[]::new);
    }
}
