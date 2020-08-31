package com.apgsga.microservice.patch.core.ssh.jenkins;

import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;
import java.util.Map;

public class JenkinsSshBuildJobCmd extends JenkinsSshCommand {

    private String jobName;

    private Map<String,String> jobParameters;

    private Map<String,File> fileParams;

    private boolean waitForJobToBeFinish;

    private boolean waitForJobToStart;

    public JenkinsSshBuildJobCmd(String jenkinsHost, String jenkinsSshPort, String jenkinsSshUser, String jobName, boolean waitForJobToStart, boolean waitForJobToBeFinish) {
        super(jenkinsHost, jenkinsSshPort, jenkinsSshUser);
        this.jobName = jobName;
        this.waitForJobToStart = waitForJobToStart;
        this.waitForJobToBeFinish = waitForJobToBeFinish;
    }

    public JenkinsSshBuildJobCmd(String jenkinsHost, String jenkinsSshPort, String jenkinsSshUser, String jobName, Map<String,String> jobParameters ,Map<String,File> fileParams, boolean waitFoJobToStart, boolean waitForJobToBeFinish) {
        super(jenkinsHost, jenkinsSshPort, jenkinsSshUser);
        this.jobName = jobName;
        this.jobParameters = jobParameters;
        this.fileParams = fileParams;
        this.waitForJobToStart = waitFoJobToStart;
        this.waitForJobToBeFinish = waitForJobToBeFinish;
    }

    @Override
    protected String[] getJenkinsCmd() {
        List<String> cmd = Lists.newArrayList();
        cmd.add("build");
        cmd.add(jobName);

        if (jobParameters != null && !jobParameters.isEmpty()) {
            for (String key : jobParameters.keySet()) {
                cmd.add("-p");
                cmd.add(key + "=" + jobParameters.get(key));
            }
        }

        if(fileParams != null && !fileParams.isEmpty()) {

        }

        if(waitForJobToBeFinish) {
            cmd.add("-f");
        }

        if(waitForJobToStart) {
            cmd.add("-w");
        }

        return cmd.stream().toArray(String[]::new);

    }
}