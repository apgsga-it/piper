package com.apgsga.microservice.patch.core.commands.jenkins.curl;

import com.apgsga.microservice.patch.core.commands.CommandBaseImpl;

import java.util.Arrays;
import java.util.stream.Stream;

public abstract class JenkinsCurlCommand extends CommandBaseImpl {

    protected final String JENKINS_SSH_USER;

    protected final String JENKINS_SSH_USER_PWD;

    protected final String JENKINS_URL;

    public JenkinsCurlCommand(String jenkinsUrl, String jenkinsUserName, String jenkinsUserPwd) {
        super();
        this.JENKINS_URL = jenkinsUrl;
        this.JENKINS_SSH_USER = jenkinsUserName;
        this.JENKINS_SSH_USER_PWD = jenkinsUserPwd;
    }

    public static JenkinsCurlCommand createJenkinsCurlGetLastBuildCmd(String jenkinsUrl, String jenkinsUserName, String jenkinsUserPwd, String jobName) {
        return new JenkinsCurlGetLastBuildCmd(jenkinsUrl,jenkinsUserName,jenkinsUserPwd,jobName);
    }

    public static JenkinsCurlCommand createJenkinsCurlGetJobInputStatus(String jenkinsUrl, String jenkinsUserName, String jenkinsUserPwd, String jobName) {
        return new JenkinsCurlGetInputStatus(jenkinsUrl,jenkinsUserName,jenkinsUserPwd,jobName);
    }

    public static JenkinsCurlCommand createJenkinsCurlSubmitPipelineInput(String jenkinsUrl, String jenkinsUserName, String jenkinsUserPwd, String jobName, String jobExecutionNumber, String inputId, String action) {
        return new JenkinsCurlSubmitPipelineInput(jenkinsUrl,jenkinsUserName,jenkinsUserPwd,jobName,jobExecutionNumber,inputId,action);
    }

    @Override
    protected String[] getParameterAsArray() {
        return Stream.concat(Arrays.stream(getFirstPart()), Arrays.stream(getCurlCmd()))
                .toArray(String[]::new);
    }

    @Override
    protected String getParameterSpaceSeperated() {
        String[] processParm = Stream.concat(Arrays.stream(getFirstPart()), Arrays.stream(getCurlCmd()))
                .toArray(String[]::new);
        return String.join(" ", processParm);
    }

    private String[] getFirstPart() {
            return new String[]{"curl", "-s", "-u", JENKINS_SSH_USER +":"+ JENKINS_SSH_USER_PWD};
    }

    protected abstract String[] getCurlCmd();
}
