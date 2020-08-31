package com.apgsga.microservice.patch.core.commands.jenkins.ssh.test;

import com.apgsga.microservice.patch.core.commands.CommandRunner;
import com.apgsga.microservice.patch.core.commands.ProcessBuilderCmdRunnerFactory;
import com.apgsga.microservice.patch.core.commands.jenkins.curl.JenkinsCurlCommand;
import groovy.json.JsonSlurper;
import org.junit.Test;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

public class JenkinsCurlCommandsTest extends JenkinsCliBaseTest {

    @Test
    public void testJenkinsCurlGetLastBuildNumber() {
        JenkinsCurlCommand jenkinsCurlCmd = JenkinsCurlCommand.createJenkinsCurlGetLastBuildCmd(JENKINS_URL,JENKINS_SSH_USER,JENKINS_SSH_USER,JOB_NAME_WITHOUT_FILE_PARAM);
        ProcessBuilderCmdRunnerFactory runnerFactory = new ProcessBuilderCmdRunnerFactory();
        CommandRunner runner = runnerFactory.create();
        List<String> result = runner.run(jenkinsCurlCmd);
        JsonSlurper js = new JsonSlurper();
        Map parsedData = (Map) js.parse(new ByteArrayInputStream(result.get(0).getBytes()));
        Map lastBuild = (Map) parsedData.get("lastBuild");
        Assert.isTrue(Integer.valueOf(String.valueOf(lastBuild.get("number"))) > 0,"Last Build number couldn not be corrrectly retrieved");
    }
}
