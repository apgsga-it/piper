package com.apgsga.microservice.patch.core.commands.jenkins.ssh.test;

import com.apgsga.microservice.patch.core.commands.CommandRunner;
import com.apgsga.microservice.patch.core.commands.ProcessBuilderCmdRunnerFactory;
import com.apgsga.microservice.patch.core.commands.jenkins.curl.JenkinsCurlCommand;
import com.apgsga.microservice.patch.core.commands.jenkins.ssh.JenkinsSshCommand;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class JenkinsCurlCommandsTest extends JenkinsCliBaseTest {

    @Test
    public void test() {

        JenkinsCurlCommand jenkinsCurlCmd = JenkinsCurlCommand.createJenkinsCurlGetLastBuildCmd(JENKINS_URL,JENKINS_SSH_USER,JENKINS_SSH_USER,JOB_NAME_WITHOUT_FILE_PARAM);
        ProcessBuilderCmdRunnerFactory runnerFactory = new ProcessBuilderCmdRunnerFactory();
        CommandRunner runner = runnerFactory.create();
        List<String> result = runner.run(jenkinsCurlCmd);
        System.out.println(result);

        //Assert.assertTrue("Returned message should contain SUCCESS",result.stream().anyMatch(c -> {return c.contains("Started " + JOB_NAME_WITHOUT_FILE_PARAM + " #");}));
    }
}
