package com.apgsga.microservice.patch.core.commands.jenkins.ssh.test;

import com.google.common.collect.Lists;

import java.util.List;

public abstract class JenkinsCliBaseTest {

    public static final String JENKINS_SSH_USER = "jhe";
    public static final String JENKINS_USER_TOKEN = "1121097812220766a1129ba16a294a489e";
    public static final String JENKINS_HOST = "192.168.26.197";
    public static final String JENKINS_PORT = "8080";
    public static final String JENKINS_SSH_PORT = "53801";
    public static final String JENKINS_URL = "http://" + JENKINS_HOST + ":" + JENKINS_PORT;
    public static final String JOB_NAME_WITHOUT_FILE_PARAM = "PatchBuilder";
    public static final String JOB_NAME_WITH_FILE_PARAM = "BuildPipelineTest";
    public static List<String> SSH_CMD = Lists.newArrayList("ssh");
    public static List<String> CURL_CMD = Lists.newArrayList("curl");
}
