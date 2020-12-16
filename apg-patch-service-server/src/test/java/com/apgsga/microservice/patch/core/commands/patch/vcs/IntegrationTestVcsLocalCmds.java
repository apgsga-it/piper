package com.apgsga.microservice.patch.core.commands.patch.vcs;

import com.apgsga.microservice.patch.core.commands.ProcessBuilderCmdRunnerFactory;
import com.apgsga.microservice.patch.core.commands.CommandRunner;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;

public class IntegrationTestVcsLocalCmds {

	@Ignore("TODO make preconditions = cvs setup automatic")
	@Test
	public void testCreatePatchBranch() {
		RandomUtils.nextInt(); 
		CommandRunner runner = new ProcessBuilderCmdRunnerFactory().create();
		runner.run(PatchSshCommand.createCreatePatchBranchCmd("someBranch" + RandomUtils.nextInt(), "HEAD", "-d /home/che/local/cvs ",
				Lists.newArrayList("module1", "module2")));
	}
	
	@Ignore("TODO make preconditions = cvs setup automatic")
	@Test
	public void testCreatePatchTag() {
		CommandRunner runner = new ProcessBuilderCmdRunnerFactory().create();
		runner.run(PatchSshCommand.createTagPatchModulesCmd("someTag" + RandomUtils.nextInt(), "HEAD", "-d /home/che/local/cvs ",
				Lists.newArrayList("module1", "module2")));
	}
	
	@Ignore("TODO make preconditions = cvs setup automatic")
	@Test
	public void testDiff() {
		CommandRunner runner = new ProcessBuilderCmdRunnerFactory().create();
		runner.run(PatchSshCommand.createDiffPatchModulesCmd("testBranch", "HEAD", "-d /home/che/local/cvs ",
				Lists.newArrayList("module1", "module2")));
	}

}
