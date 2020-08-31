package com.apgsga.microservice.patch.core.ssh.patch.vcs;

import com.apgsga.microservice.patch.core.ssh.LoggingMockSshRunnerFactory;
import com.apgsga.microservice.patch.core.ssh.SshCommand;
import com.apgsga.microservice.patch.core.ssh.SshCommandRunner;
import com.apgsga.microservice.patch.core.ssh.jenkins.JenkinsSshCommand;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;

import com.google.common.collect.Lists;

public class IntegrationTestVcsMockCmds {

	@Test
	public void testCreatePatchBranch() throws Exception {
		SshCommandRunner runner = new LoggingMockSshRunnerFactory().create();
		runner.preProcess();
		SshCommand cmd = PatchSshCommand.createCreatePatchBranchCmd("someBranch" + RandomUtils.nextInt(), "HEAD", "-d /home/che/local/cvs ",
				Lists.newArrayList("module1", "module2"));
		runner.run(cmd);
		runner.postProcess();
	}
	
	@Test
	public void testCreatePatchTag() throws Exception {
		SshCommandRunner runner = new LoggingMockSshRunnerFactory().create();
		runner.preProcess();
		SshCommand cmd = PatchSshCommand.createTagPatchModulesCmd("someTag" + RandomUtils.nextInt(), "HEAD", "-d /home/che/local/cvs ",
				Lists.newArrayList("module1", "module2"));
		runner.run(cmd);
		runner.postProcess();
	}
	
	@Test
	public void testDiff() throws Exception {
		SshCommandRunner runner = new LoggingMockSshRunnerFactory().create();
		runner.preProcess();
		SshCommand cmd = PatchSshCommand.createDiffPatchModulesCmd("testBranch", "HEAD", "-d /home/che/local/cvs ",
				Lists.newArrayList("module1", "module2"));
		runner.run(cmd);
		runner.run(cmd);
		runner.postProcess();
	}

}
