package com.apgsga.microservice.patch.core.commands.jenkins.ssh;

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
    protected boolean hasFileParam() {
        return fileParams != null && !fileParams.isEmpty();
    }

    @Override
    protected String getFileNameParameter() {
        if(hasFileParam()) {
            return fileParams.get(fileParams.keySet().toArray()[0]).getName();
        }
        return null;
    }

    @Override
    protected String[] getJenkinsCmd() {

        List<String> tmpCmd = Lists.newArrayList();


        String s = "cat /home/jhe/Patch0.json | ssh -l" + jenkinsSshUser + " -p " + jenkinsSshPort + " " + jenkinsHost + "build Patch1 -p patchFile.json=";

        System.out.println("getJenkinsCmd, s => " + s);

        tmpCmd.add("/bin/sh");
        tmpCmd.add("-c");
        tmpCmd.add(s);

        return tmpCmd.stream().toArray(String[]::new);

// TODO JHE (01.10.2020) : do not forget to put that correct again
        /*
        List<String> cmd = Lists.newArrayList();
        cmd.add("build");
        cmd.add(jobName);

        if (jobParameters != null && !jobParameters.isEmpty()) {
            for (String key : jobParameters.keySet()) {
                cmd.add("-p");
                cmd.add(key + "=" + jobParameters.get(key));
            }
        }

        if(hasFileParam()) {
            String fileParamName = (String) fileParams.keySet().toArray()[0];
            cmd.add("-p");
            cmd.add(fileParamName + "=");

        }

        if(waitForJobToBeFinish) {
            cmd.add("-f");
        }

        if(waitForJobToStart) {
            cmd.add("-w");
        }

        return cmd.stream().toArray(String[]::new);

        */

    }
}