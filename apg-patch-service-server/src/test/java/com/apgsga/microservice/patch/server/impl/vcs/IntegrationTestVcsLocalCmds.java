package com.apgsga.microservice.patch.server.impl.vcs;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;

public class IntegrationTestVcsLocalCmds {

	@Ignore("TODO make preconditions = cvs setup automatic")
	@Test
	public void testCreatePatchBranch() throws Exception {
		RandomUtils.nextInt(); 
		VcsCommandRunner runner = new ProcessBuilderCmdRunnerFactory().create();
		runner.run(PatchVcsCommand.createCreatePatchBranchCmd("someBranch" + RandomUtils.nextInt(), "HEAD", "-d /home/che/local/cvs ",
				Lists.newArrayList("module1", "module2")));
	}
	
	@Ignore("TODO make preconditions = cvs setup automatic")
	@Test
	public void testCreatePatchTag() throws Exception {
		VcsCommandRunner runner = new ProcessBuilderCmdRunnerFactory().create();
		runner.run(PatchVcsCommand.createTagPatchModulesCmd("someTag" + RandomUtils.nextInt(), "HEAD", "-d /home/che/local/cvs ",
				Lists.newArrayList("module1", "module2")));
	}
	
	@Ignore("TODO make preconditions = cvs setup automatic")
	@Test
	public void testDiff() throws Exception {
		VcsCommandRunner runner = new ProcessBuilderCmdRunnerFactory().create();
		runner.run(PatchVcsCommand.createDiffPatchModulesCmd("testBranch", "HEAD", "-d /home/che/local/cvs ",
				Lists.newArrayList("module1", "module2")));
	}

}
