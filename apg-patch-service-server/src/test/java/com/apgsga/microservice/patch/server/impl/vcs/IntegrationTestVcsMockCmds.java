package com.apgsga.microservice.patch.server.impl.vcs;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;

public class IntegrationTestVcsMockCmds {

	@Test
	public void testCreatePatchBranch() throws Exception {
		VcsCommandRunner runner = new LoggingMockVcsRunnerFactory().create();
		runner.preProcess();
		VcsCommand cmd = PatchVcsCommand.createCreatePatchBranchCmd("someBranch" + RandomUtils.nextInt(), "HEAD", "-d /home/che/local/cvs ",
				Lists.newArrayList("module1", "module2"));
		runner.run(cmd);
		runner.postProcess();
	}
	
	@Test
	public void testCreatePatchTag() throws Exception {
		VcsCommandRunner runner = new LoggingMockVcsRunnerFactory().create();
		runner.preProcess();
		VcsCommand cmd = PatchVcsCommand.createTagPatchModulesCmd("someTag" + RandomUtils.nextInt(), "HEAD", "-d /home/che/local/cvs ",
				Lists.newArrayList("module1", "module2"));
		runner.run(cmd);
		runner.postProcess();
	}
	
	@Test
	public void testDiff() throws Exception {
		VcsCommandRunner runner = new LoggingMockVcsRunnerFactory().create();
		runner.preProcess();
		VcsCommand cmd = PatchVcsCommand.createDiffPatchModulesCmd("testBranch", "HEAD", "-d /home/che/local/cvs ",
				Lists.newArrayList("module1", "module2"));
		runner.run(cmd);
		runner.run(cmd);
		runner.postProcess();
	}

}
