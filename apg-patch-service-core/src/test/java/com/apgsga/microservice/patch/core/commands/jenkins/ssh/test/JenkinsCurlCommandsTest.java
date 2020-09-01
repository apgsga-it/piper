package com.apgsga.microservice.patch.core.commands.jenkins.ssh.test;

import com.apgsga.microservice.patch.core.commands.CommandRunner;
import com.apgsga.microservice.patch.core.commands.ProcessBuilderCmdRunnerFactory;
import com.apgsga.microservice.patch.core.commands.jenkins.curl.JenkinsCurlCommand;
import groovy.json.JsonSlurper;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Ignore
public class JenkinsCurlCommandsTest extends JenkinsCliBaseTest {

    @Test
    public void testJenkinsCurlGetLastBuildNumber() {
        JenkinsCurlCommand jenkinsCurlCmd = JenkinsCurlCommand.createJenkinsCurlGetLastBuildCmd(JENKINS_URL,JENKINS_SSH_USER,JENKINS_USER_TOKEN,JOB_NAME_WITHOUT_FILE_PARAM);
        ProcessBuilderCmdRunnerFactory runnerFactory = new ProcessBuilderCmdRunnerFactory();
        CommandRunner runner = runnerFactory.create();
        List<String> result = runner.run(jenkinsCurlCmd);
        JsonSlurper js = new JsonSlurper();
        Map parsedData = (Map) js.parse(new ByteArrayInputStream(result.get(0).getBytes()));
        Map lastBuild = (Map) parsedData.get("lastBuild");
        Assert.isTrue(Integer.valueOf(String.valueOf(lastBuild.get("number"))) > 0,"Last Build number couldn not be corrrectly retrieved");
    }

    @Test
    public void testLastJenkinsJobWasSuccessful() {
        JenkinsCurlCommand jenkinsCurlCmd = JenkinsCurlCommand.createJenkinsCurlGetLastBuildCmd(JENKINS_URL,JENKINS_SSH_USER,JENKINS_USER_TOKEN,JOB_NAME_WITHOUT_FILE_PARAM);
        ProcessBuilderCmdRunnerFactory runnerFactory = new ProcessBuilderCmdRunnerFactory();
        CommandRunner runner = runnerFactory.create();
        List<String> result = runner.run(jenkinsCurlCmd);
        JsonSlurper js = new JsonSlurper();
        Map parsedData = (Map) js.parse(new ByteArrayInputStream(result.get(0).getBytes()));
        Map lastBuild = (Map) parsedData.get("lastBuild");
        Integer lastBuildNumber = Integer.valueOf(String.valueOf(lastBuild.get("number")));
        Map lastSuccessfulBuild = (Map) parsedData.get("lastSuccessfulBuild");
        Integer lastSuccessfulBuildNumber = Integer.valueOf(String.valueOf(lastSuccessfulBuild.get("number")));
        Assert.isTrue(lastBuildNumber == lastSuccessfulBuildNumber, "Last Build was not successful");
    }

    @Test
    public void testIsJobWaitingOnInput() {
        JenkinsCurlCommand jenkinsCurlCommand = JenkinsCurlCommand.createJenkinsCurlGetJobInputStatus(JENKINS_URL,JENKINS_SSH_USER,JENKINS_USER_TOKEN,"PipelineWithInputStep");
        ProcessBuilderCmdRunnerFactory runnerFactory = new ProcessBuilderCmdRunnerFactory();
        CommandRunner runner = runnerFactory.create();
        List<String> result = runner.run(jenkinsCurlCommand);
        JsonSlurper js = new JsonSlurper();
        ArrayList<Map> parsedData = (ArrayList<Map>) js.parse(new ByteArrayInputStream(result.get(0).getBytes()));
        Map lastBuiltState = (Map) parsedData.get(0);
        System.out.println("Last Build status = " + lastBuiltState.get("status"));
        System.out.println("DONE");
    }

    @Test
    public void testSubmitInputActionToPipeline() {
        JenkinsCurlCommand jenkinsCurlCommand = JenkinsCurlCommand.createJenkinsCurlSubmitPipelineInput(JENKINS_URL,JENKINS_SSH_USER,JENKINS_USER_TOKEN,"PipelineWithInputStep","10","Oktocontinue","abort");
        ProcessBuilderCmdRunnerFactory runnerFactory = new ProcessBuilderCmdRunnerFactory();
        CommandRunner runner = runnerFactory.create();
        List<String> result = runner.run(jenkinsCurlCommand);
        System.out.println(result);
        System.out.println("DONE");
    }
}
