package com.apgsga.microservice.patch.core.commands.jenkins.curl;

import com.google.common.collect.Lists;

import java.util.List;

public class JenkinsCurlGetInputStatus extends JenkinsCurlCommand {

    private String jobName;

    public JenkinsCurlGetInputStatus(String jenkinsUrl, String jenkinsUserName, String jenkinsUserPwd, String jobName) {
        super(jenkinsUrl,jenkinsUserName,jenkinsUserPwd);
        this.jobName = jobName;
    }

    @Override
    protected String[] getCurlCmd() {
        List<String> cmd = Lists.newArrayList();
        cmd.add(JENKINS_URL + "/job/" + jobName + "/wfapi/runs");
        return cmd.stream().toArray(String[]::new);
    }
}
