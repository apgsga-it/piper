package com.apgsga.microservice.patch.core.commands.jenkins.curl;

import com.google.common.collect.Lists;

import java.util.List;

public class JenkinsCurlGetLastBuildCmd extends JenkinsCurlCommand {

    private final String jobName;

    public JenkinsCurlGetLastBuildCmd(String jenkinsUrl, String jenkinsUserName, String jenkinsUserPwd, String jobName) {
        super(jenkinsUrl,jenkinsUserName,jenkinsUserPwd);
        this.jobName = jobName;
    }

    @Override
    protected String[] getCurlCmd() {
        List<String> cmd = Lists.newArrayList();
        cmd.add(JENKINS_URL + "/job/" + jobName + "/api/json");
        return cmd.toArray(new String[0]);
    }
}
