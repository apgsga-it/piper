package com.apgsga.microservice.patch.core.commands.jenkins.ssh;

import com.google.common.collect.Lists;

import java.util.List;

public class JenkinsSshStopBuildCmd extends JenkinsSshCommand {

    private boolean waitForJobToBeFinish;

    private boolean waitForJobToStart;

    private String jobName;

    public JenkinsSshStopBuildCmd(String jenkinsHost, String jenkinsSshPort, String jenkinsSshUser, String jobName, boolean waitForJobToStart, boolean waitForJobToBeFinish) {
        super(jenkinsHost, jenkinsSshPort, jenkinsSshUser);
        this.jobName = jobName;
        this.waitForJobToStart = waitForJobToStart;
        this.waitForJobToBeFinish = waitForJobToBeFinish;
    }

    @Override
    protected boolean hasFileParam() {
        return false;
    }

    @Override
    protected String getFileParameterName() {
        return null;
    }

    @Override
    protected String[] getJenkinsCmd() {
        List<String> cmd = Lists.newArrayList();
        cmd.add("stop-builds");
        cmd.add(jobName);

        if(waitForJobToBeFinish) {
            cmd.add("-f");
        }

        if(waitForJobToStart) {
            cmd.add("-w");
        }

        return cmd.stream().toArray(String[]::new);
    }
}
