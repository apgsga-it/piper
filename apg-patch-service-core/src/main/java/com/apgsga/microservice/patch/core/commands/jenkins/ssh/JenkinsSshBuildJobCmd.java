package com.apgsga.microservice.patch.core.commands.jenkins.ssh;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.File;
import java.util.List;
import java.util.Map;

public class JenkinsSshBuildJobCmd extends JenkinsSshCommand {

    private String jobName;

    private Map<String,String> jobParameters;

    private Map<String,String> fileParams = Maps.newHashMap();

    private boolean waitForJobToBeFinish;

    private boolean waitForJobToStart;

    public JenkinsSshBuildJobCmd(String jenkinsHost, String jenkinsSshPort, String jenkinsSshUser, String jobName, boolean waitForJobToStart, boolean waitForJobToBeFinish) {
        super(jenkinsHost, jenkinsSshPort, jenkinsSshUser);
        this.jobName = jobName;
        this.waitForJobToStart = waitForJobToStart;
        this.waitForJobToBeFinish = waitForJobToBeFinish;
    }

    public JenkinsSshBuildJobCmd(String jenkinsHost, String jenkinsSshPort, String jenkinsSshUser, String jobName, Map<String,String> jobParameters ,Map<String,String> fileParams, boolean waitFoJobToStart, boolean waitForJobToBeFinish) {
        super(jenkinsHost, jenkinsSshPort, jenkinsSshUser);
        this.jobName = jobName;
        this.jobParameters = jobParameters;
        this.fileParams = fileParams;
        this.waitForJobToStart = waitFoJobToStart;
        this.waitForJobToBeFinish = waitForJobToBeFinish;
    }

    public JenkinsSshBuildJobCmd(String jenkinsHost, String jenkinsSshPort, String jenkinsSshUser, String jobName, Map<String,String> jobParameters , boolean waitFoJobToStart, boolean waitForJobToBeFinish) {
        super(jenkinsHost, jenkinsSshPort, jenkinsSshUser);
        this.jobName = jobName;
        this.jobParameters = jobParameters;
        this.waitForJobToStart = waitFoJobToStart;
        this.waitForJobToBeFinish = waitForJobToBeFinish;
    }

    @Override
    protected boolean hasFileParam() {
        return fileParams != null && !fileParams.isEmpty();
    }

    @Override
    protected String getFileParameterName() {
        if(hasFileParam()) {
            return fileParams.keySet().stream().findFirst().get();
        }
        return null;
    }

    private String getFileParameterValue() {
        if(hasFileParam()) {
            return fileParams.get(getFileParameterName());
        }
        return null;
    }

    @Override
    protected String[] getJenkinsCmd() {

        // JHE (01.10.2020): in the "cat" scenario, we have to provide the command in a "single shot".
        //                      see also : https://stackoverflow.com/questions/3776195/using-java-processbuilder-to-execute-a-piped-command
        //                      we're encountering the same behavior, but with "cat" command

        if(hasFileParam()) {
            List<String> tmpCmd = Lists.newArrayList();
            String catCmd = "cat " + getFileParameterValue() + " | ssh -l " + jenkinsSshUser + " -p " + jenkinsSshPort + " " + jenkinsHost + " build " + jobName + " -p " + getFileParameterName() + "=";
            if (jobParameters != null && !jobParameters.isEmpty()) {
                for (String key : jobParameters.keySet()) {
                    catCmd += " -p " + key + "=" + jobParameters.get(key);
                }
            }
            tmpCmd.add("/bin/sh");
            tmpCmd.add("-c");
            tmpCmd.add(catCmd);

            //TODO JHE (01.10.2020): eventually add -w and -f options, but not sure that would work

            return tmpCmd.stream().toArray(String[]::new);
        }
        else {

            List<String> cmd = Lists.newArrayList();
            cmd.add("build");
            cmd.add(jobName);

            if (jobParameters != null && !jobParameters.isEmpty()) {
                for (String key : jobParameters.keySet()) {
                    cmd.add("-p");
                    cmd.add(key + "=" + jobParameters.get(key));
                }
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
}