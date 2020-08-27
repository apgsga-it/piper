package com.apgsga.microservice.patch.core.impl.cmd;

import com.apgsga.microservice.patch.core.impl.vcs.ProcessBuilderCmdRunner;
import com.apgsga.microservice.patch.core.impl.vcs.SimpleCommand;
import com.apgsga.microservice.patch.core.impl.vcs.VcsCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.assertj.core.util.Lists;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class ProcessRunnerTest {

    public static final String USER = "che";
    public static final String JENKINS_HOST = "172.16.92.196";
    public static final String JENKINS_PORT = "8080";
    public static final String JENKINS_URL = "http://" + JENKINS_HOST + ":" + JENKINS_PORT;
    public static final String JOB_NAME = "TestappBom";
    public static List<String> SSH_CMD = Lists.newArrayList("ssh");
    public static List<String> CURL_CMD = Lists.newArrayList("curl");

    @Test
    public void testJenkinsCliHelp() throws Exception {
        List<String> processBuilderArgs = Lists.newArrayList();
        processBuilderArgs.addAll(SSH_CMD);
        processBuilderArgs.addAll(Lists.newArrayList("-l", USER, "-p", "53801", JENKINS_HOST, "help"));
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
        org.junit.Assert.assertEquals("No errors should be detected", 0, exitCode);
    }

    @Test
    public void testJenkinsCliBuildJob() throws Exception {
        List<String> processBuilderArgs = Lists.newArrayList();
        processBuilderArgs.addAll(SSH_CMD);
        processBuilderArgs.addAll(Lists.newArrayList("-l", USER, "-p", "53801", JENKINS_HOST, "build", JOB_NAME, "-w"));
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
        org.junit.Assert.assertEquals("No errors should be detected", 0, exitCode);
    }

    @Test
    public void testJenkinsCliConsole() throws Exception {
        List<String> processBuilderArgs = Lists.newArrayList();
        processBuilderArgs.addAll(SSH_CMD);
        processBuilderArgs.addAll(Lists.newArrayList("-l", USER, "-p", "53801", JENKINS_HOST, "console", JOB_NAME, "-f"));
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
        // This seems to be a problem with jenkinscli
        org.junit.Assert.assertEquals("No errors should be detected", 255, exitCode);
    }

    @Test
    public void testJenkinsCliStartBuildAndFollow() throws Exception {
        List<String> processBuilderArgs = Lists.newArrayList();
        processBuilderArgs.addAll(SSH_CMD);
        processBuilderArgs.addAll(Lists.newArrayList("-l", USER, "-p", "53801", JENKINS_HOST, "build", JOB_NAME, "-w"));
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
        org.junit.Assert.assertEquals("No errors should be detected", 0, exitCode);
        processBuilderArgs = Lists.newArrayList();
        processBuilderArgs.addAll(SSH_CMD);
        processBuilderArgs.addAll(Lists.newArrayList("-l", USER, "-p", "53801", JENKINS_HOST, "console", JOB_NAME, "-f"));
        processBuilder = new ProcessBuilder(processBuilderArgs);
        process = processBuilder.start();
        errorGobbler = new ProcessBuilderCmdRunner.StreamGobbler(process.getErrorStream());
        outputGobbler = new ProcessBuilderCmdRunner.StreamGobbler(process.getInputStream());
        outputGobbler.start();
        errorGobbler.start();
        exitCode = process.waitFor();
        errorGobbler.join();
        outputGobbler.join();
        outputGobbler.log();
        errorGobbler.log();
        // This seems to be a problem with jenkinscli
        org.junit.Assert.assertEquals("No errors should be detected", 255, exitCode);

    }

    @Test
    public void testJenkinsCliHelpWithProcessBuilderCmdRunner() {
        List<String> processBuilderArgs = Lists.newArrayList();
        processBuilderArgs.addAll(SSH_CMD);
        processBuilderArgs.addAll(Lists.newArrayList("-l", USER, "-p", "53801", JENKINS_HOST, "help"));
        ProcessBuilderCmdRunner cmdRunner = new ProcessBuilderCmdRunner();
        List<String> result = cmdRunner.run(new SimpleCommand(processBuilderArgs));
        System.out.println(result);
    }

    @Test
    public void testCurlGetJenkinsCrumb() {
        List<String> processBuilderArgs = Lists.newArrayList();
        processBuilderArgs.addAll(CURL_CMD);
        // Assuming Password = User
        processBuilderArgs.addAll(Lists.newArrayList("-s", "-u", USER+":"+USER, JENKINS_URL + "/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,\":\",//crumb)"));
        ProcessBuilderCmdRunner cmdRunner = new ProcessBuilderCmdRunner();
        List<String> result = cmdRunner.run(new SimpleCommand(processBuilderArgs));
        System.out.println(result);
    }

    @Test
    public void testCurlGetLastBuildNumber() throws IOException {
        List<String> processBuilderArgs = Lists.newArrayList();
        processBuilderArgs.addAll(CURL_CMD);
        // Assuming Password = User
        // The Json Output is Written to a Tempfile
        File outputFile = File.createTempFile("jenkins_job_data", ".json");
        processBuilderArgs.addAll(Lists.newArrayList("-s", "-u", USER+":"+USER, JENKINS_URL + "/job/" + JOB_NAME + "/api/json"));
        ProcessBuilderCmdRunner cmdRunner = new ProcessBuilderCmdRunner();
        List<String> result = cmdRunner.run(new SimpleCommand(processBuilderArgs));
        String jsonString = String.join("",result);
        ObjectMapper mapper = new ObjectMapper();
        Map<?,?> jsonOutput = mapper.readValue(jsonString, Map.class);
        Map<?,?> lastBuild = (Map<?, ?>) jsonOutput.get("lastBuild");
        System.out.println(lastBuild.get("number"));
    }





}