package com.apgsga.microservice.patch.core.commands.jenkins.curl;

import com.apgsga.microservice.patch.core.commands.CommandBaseImpl;

import java.util.Arrays;
import java.util.stream.Stream;

public abstract class JenkinsCurlCommand extends CommandBaseImpl {

    protected String JENKINS_SSH_USER;

    protected String JENKINS_SSH_USER_PWD;

    protected String JENKINS_URL;

    public JenkinsCurlCommand(String jenkinsUrl, String jenkinsUserName, String jenkinsUserPwd) {
        super();
        this.JENKINS_URL = jenkinsUrl;
        this.JENKINS_SSH_USER = jenkinsUserName;
        this.JENKINS_SSH_USER_PWD = jenkinsUserPwd;
    }

    public static JenkinsCurlCommand createJenkinsCurlGetLastBuildCmd(String jenkinsUrl, String jenkinsUserName, String jenkinsUserPwd, String jobName) {
        return new JenkinsCurlGetLastBuildCmd(jenkinsUrl,jenkinsUserName,jenkinsUserPwd,jobName);
    }

    @Override
    protected String[] getParameterAsArray() {
        String[] parameter = Stream.concat(Arrays.stream(getFirstPart()), Arrays.stream(getCurlCmd()))
                .toArray(String[]::new);
        return parameter;
    }

    @Override
    protected String getParameterSpaceSeperated() {
        String[] processParm = Stream.concat(Arrays.stream(getFirstPart()), Arrays.stream(getCurlCmd()))
                .toArray(String[]::new);
        String parameter = String.join(" ", processParm);
        return parameter;
    }

    private String[] getFirstPart() {
            return new String[]{"curl", "-s", "-u", JENKINS_SSH_USER +":"+ JENKINS_SSH_USER_PWD};
    }

    protected abstract String[] getCurlCmd();
}
