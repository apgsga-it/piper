package com.apgsga.microservice.patch.core.commands.jenkins.ssh.test;

import com.apgsga.microservice.patch.core.commands.CommandRunner;
import com.apgsga.microservice.patch.core.commands.ProcessBuilderCmdRunnerFactory;
import com.apgsga.microservice.patch.core.commands.jenkins.ssh.JenkinsSshCommand;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Map;

@Ignore
public class JenkinsSshCommandsTest extends JenkinsCliBaseTest {

    @Test
    public void testJenkinsSshBuildCommandWithoutWaiting() {
        JenkinsSshCommand jenkinsSshCommand = JenkinsSshCommand.createJenkinsSshBuildJobAndReturnImmediatelyCmd(JENKINS_HOST,JENKINS_SSH_PORT, JENKINS_SSH_USER, JOB_NAME_WITHOUT_FILE_PARAM);
        ProcessBuilderCmdRunnerFactory runnerFactory = new ProcessBuilderCmdRunnerFactory();
        CommandRunner runner = runnerFactory.create();
        List<String> result = runner.run(jenkinsSshCommand);
        Assert.assertTrue("Returned message should be empty as we did not wait",result.isEmpty());
    }

    @Test
    public void testJenkinsSshBuildWithParameterCommandWithoutWaiting() {
        Map<String,String> params = Maps.newHashMap();
        params.put("patchnumber","2222");
        JenkinsSshCommand jenkinsSshCommand = JenkinsSshCommand.createJenkinsSshBuildJobAndReturnImmediatelyCmd(JENKINS_HOST,JENKINS_SSH_PORT, JENKINS_SSH_USER, JOB_NAME_WITHOUT_FILE_PARAM,params,null);
        ProcessBuilderCmdRunnerFactory runnerFactory = new ProcessBuilderCmdRunnerFactory();
        CommandRunner runner = runnerFactory.create();
        List<String> result = runner.run(jenkinsSshCommand);
        Assert.assertTrue("Returned message should be empty as we did not wait",result.isEmpty());
    }

    @Test
    public void testJenkinsSshBuildCommandWithWaiting() {
        JenkinsSshCommand jenkinsSshCommand = JenkinsSshCommand.createJenkinsSshBuildJobAndWaitForCompleteCmd(JENKINS_HOST,JENKINS_SSH_PORT, JENKINS_SSH_USER, JOB_NAME_WITHOUT_FILE_PARAM);
        ProcessBuilderCmdRunnerFactory runnerFactory = new ProcessBuilderCmdRunnerFactory();
        CommandRunner runner = runnerFactory.create();
        List<String> result = runner.run(jenkinsSshCommand);
        Assert.assertTrue("Returned message should contain SUCCESS",result.stream().anyMatch(c -> c.contains("SUCCESS")));
    }

    @Test
    public void testJenkinsSshBuildWithParameterCommandWithWaiting() {
        Map<String,String> params = Maps.newHashMap();
        params.put("patchnumber","2222");
        JenkinsSshCommand jenkinsSshCommand = JenkinsSshCommand.createJenkinsSshBuildJobAndWaitForCompleteCmd(JENKINS_HOST,JENKINS_SSH_PORT, JENKINS_SSH_USER, JOB_NAME_WITHOUT_FILE_PARAM,params);
        ProcessBuilderCmdRunnerFactory runnerFactory = new ProcessBuilderCmdRunnerFactory();
        CommandRunner runner = runnerFactory.create();
        List<String> result = runner.run(jenkinsSshCommand);
        Assert.assertTrue("Returned message should contain SUCCESS",result.stream().anyMatch(c -> c.contains("SUCCESS")));
    }

    @Test
    public void testJenkinsSshBuildJobCmdAndWaitForStart() {
        JenkinsSshCommand jenkinsSshCommand = JenkinsSshCommand.createJenkinsSshBuildJobAndWaitForStartCmd(JENKINS_HOST,JENKINS_SSH_PORT, JENKINS_SSH_USER, JOB_NAME_WITHOUT_FILE_PARAM);
        ProcessBuilderCmdRunnerFactory runnerFactory = new ProcessBuilderCmdRunnerFactory();
        CommandRunner runner = runnerFactory.create();
        List<String> result = runner.run(jenkinsSshCommand);
        Assert.assertTrue("Returned message should contain SUCCESS",result.stream().anyMatch(c -> c.contains("Started " + JOB_NAME_WITHOUT_FILE_PARAM + " #")));
    }

    @Test
    public void testJenkinsSshBuildJobWithParameterCmdAndWaitForStart() {
        Map<String,String> params = Maps.newHashMap();
        params.put("patchnumber","2222");
        JenkinsSshCommand jenkinsSshCommand = JenkinsSshCommand.createJenkinsSshBuildJobAndWaitForStartCmd(JENKINS_HOST, JENKINS_SSH_PORT, JENKINS_SSH_USER, JOB_NAME_WITHOUT_FILE_PARAM, params,null);
        ProcessBuilderCmdRunnerFactory runnerFactory = new ProcessBuilderCmdRunnerFactory();
        CommandRunner runner = runnerFactory.create();
        List<String> result = runner.run(jenkinsSshCommand);
        Assert.assertTrue("Returned message should contain SUCCESS",result.stream().anyMatch(c -> c.contains("Started " + JOB_NAME_WITHOUT_FILE_PARAM + " #")));
    }

    @Test
    public void testJenkinsSshBuildJobWithFileParameterCmdWithoutWaiting() {
        Map<String, String> fileParams = Maps.newHashMap();
        fileParams.put("patchFile.json","src/test/resources/Patch5401.json");
        JenkinsSshCommand jenkinsSshCommand = JenkinsSshCommand.createJenkinsSshBuildJobAndReturnImmediatelyCmd(JENKINS_HOST,JENKINS_SSH_PORT,JENKINS_SSH_USER,JOB_NAME_WITH_FILE_PARAM,null,fileParams);
        ProcessBuilderCmdRunnerFactory runnerFactory = new ProcessBuilderCmdRunnerFactory();
        CommandRunner runner = runnerFactory.create();
        List<String> result = runner.run(jenkinsSshCommand);
        result.forEach(System.out::println);
        System.out.println("DONE");
    }
}
