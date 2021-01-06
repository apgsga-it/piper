package com.apgsga.microservice.patch.core.commands.patch.vcs;

import com.apgsga.microservice.patch.core.commands.Command;
import com.apgsga.microservice.patch.core.commands.CommandRunner;
import com.apgsga.microservice.patch.core.commands.LoggingMockSshRunnerFactory;
import com.google.common.collect.Lists;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;

public class IntegrationTestVcsMockCmds {

	@Test
	public void testCreatePatchBranch() {
		CommandRunner runner = new LoggingMockSshRunnerFactory().create();
		runner.preProcess();
		Command cmd = PatchSshCommand.createCreatePatchBranchCmd("someBranch" + RandomUtils.nextInt(), "HEAD", "-d /home/che/local/cvs ",
				Lists.newArrayList("module1", "module2"));
		runner.run(cmd);
		runner.postProcess();
	}
	
	@Test
	public void testCreatePatchTag() {
		CommandRunner runner = new LoggingMockSshRunnerFactory().create();
		runner.preProcess();
		Command cmd = PatchSshCommand.createTagPatchModulesCmd("someTag" + RandomUtils.nextInt(), "HEAD", "-d /home/che/local/cvs ",
				Lists.newArrayList("module1", "module2"));
		runner.run(cmd);
		runner.postProcess();
	}
	
	@Test
	public void testDiff() {
		CommandRunner runner = new LoggingMockSshRunnerFactory().create();
		runner.preProcess();
		Command cmd = PatchSshCommand.createDiffPatchModulesCmd("testBranch", "HEAD", "-d /home/che/local/cvs ",
				Lists.newArrayList("module1", "module2"));
		runner.run(cmd);
		runner.run(cmd);
		runner.postProcess();
	}

}
