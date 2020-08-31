package com.apgsga.microservice.patch.core.ssh.jenkins.test;

import com.apgsga.microservice.patch.core.ssh.ProcessBuilderCmdRunner;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class JenkinsCliCommandTests extends JenkinsCliBaseTest{

    @Test
    public void testJenkinsCliHelp() throws Exception {
        List<String> processBuilderArgs = Lists.newArrayList();
        processBuilderArgs.addAll(SSH_CMD);
        processBuilderArgs.addAll(Lists.newArrayList("-l", JENKINS_SSH_USER, "-p", JENKINS_SSH_PORT, JENKINS_HOST, "help"));
        ProcessBuilder processBuilder = new ProcessBuilder(processBuilderArgs);
        Process process = processBuilder.start();
        ProcessBuilderCmdRunner.StreamGobbler errorGobbler = new ProcessBuilderCmdRunner.StreamGobbler(process.getErrorStream());
        ProcessBuilderCmdRunner.StreamGobbler outputGobbler = new ProcessBuilderCmdRunner.StreamGobbler(process.getInputStream());
        outputGobbler.start();
        errorGobbler.start();
        int exitCode = process.waitFor();
        errorGobbler.join();
        outputGobbler.join();
        outputGobbler.log();
        errorGobbler.log();
        Assert.assertEquals("No errors should be detected", 0, exitCode);

    }

    @Test
    public void testJenkinsCliBuildJob() throws Exception {
        List<String> processBuilderArgs = Lists.newArrayList();
        processBuilderArgs.addAll(SSH_CMD);
        processBuilderArgs.addAll(Lists.newArrayList("-l", JENKINS_SSH_USER, "-p", JENKINS_SSH_PORT, JENKINS_HOST, "build", JOB_NAME_WITHOUT_FILE_PARAM, "-w"));
        ProcessBuilder processBuilder = new ProcessBuilder(processBuilderArgs);
        Process process = processBuilder.start();
        ProcessBuilderCmdRunner.StreamGobbler errorGobbler = new ProcessBuilderCmdRunner.StreamGobbler(process.getErrorStream());
        ProcessBuilderCmdRunner.StreamGobbler outputGobbler = new ProcessBuilderCmdRunner.StreamGobbler(process.getInputStream());
        outputGobbler.start();
        errorGobbler.start();
        int exitCode = process.waitFor();
        errorGobbler.join();
        outputGobbler.join();
        outputGobbler.log();
        errorGobbler.log();
        Assert.assertEquals("No errors should be detected", 0, exitCode);
    }

    @Test
    public void testCurlGetJenkinsCrumb() throws Exception {
        List<String> processBuilderArgs = Lists.newArrayList();
        processBuilderArgs.addAll(CURL_CMD);
        // Assuming Password = User
        processBuilderArgs.addAll(Lists.newArrayList("-s", "-u", JENKINS_SSH_USER +":"+ JENKINS_SSH_USER, JENKINS_URL + "/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,\":\",//crumb)"));
        ProcessBuilder processBuilder = new ProcessBuilder(processBuilderArgs);
        Process process = processBuilder.start();
        ProcessBuilderCmdRunner.StreamGobbler errorGobbler = new ProcessBuilderCmdRunner.StreamGobbler(process.getErrorStream());
        ProcessBuilderCmdRunner.StreamGobbler outputGobbler = new ProcessBuilderCmdRunner.StreamGobbler(process.getInputStream());
        outputGobbler.start();
        errorGobbler.start();
        int exitCode = process.waitFor();
        errorGobbler.join();
        outputGobbler.join();
        outputGobbler.log();
        errorGobbler.log();
        Assert.assertEquals("No errors should be detected", 0, exitCode);
    }

    @Test
    public void testCurlGetLastBuildNumber() throws Exception {
        List<String> processBuilderArgs = Lists.newArrayList();
        processBuilderArgs.addAll(CURL_CMD);
        // Assuming Password = User
        processBuilderArgs.addAll(Lists.newArrayList("-s", "-u", JENKINS_SSH_USER +":"+ JENKINS_SSH_USER, JENKINS_URL + "/job/" + JOB_NAME_WITHOUT_FILE_PARAM + "/api/json"));
        ProcessBuilder processBuilder = new ProcessBuilder(processBuilderArgs);
        Process process = processBuilder.start();
        ProcessBuilderCmdRunner.StreamGobbler errorGobbler = new ProcessBuilderCmdRunner.StreamGobbler(process.getErrorStream());
        ProcessBuilderCmdRunner.StreamGobbler outputGobbler = new ProcessBuilderCmdRunner.StreamGobbler(process.getInputStream());
        outputGobbler.start();
        errorGobbler.start();
        int exitCode = process.waitFor();
        errorGobbler.join();
        outputGobbler.join();
        outputGobbler.log();
        errorGobbler.log();
        Assert.assertEquals("No errors should be detected", 0, exitCode);
    }
}
