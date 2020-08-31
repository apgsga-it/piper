package com.apgsga.microservice.patch.core.commands.patch.vcs;

import com.apgsga.microservice.patch.core.commands.JschSessionCmdRunnerFactory;
import com.apgsga.microservice.patch.core.commands.Command;
import com.apgsga.microservice.patch.core.commands.CommandRunner;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;

public class IntegrationTestVcsSshCmds {

	@Ignore("TODO make preconditions = cvs setup automatic")
	@Test
	public void testCreatePatchBranch() throws Exception {
		// JHE (25.06.2018): Within JAVA8MIG-386, we removed vcs.password, probably this test would noe fail
		CommandRunner runner = new JschSessionCmdRunnerFactory("che","192.168.17.129").create();
		runner.preProcess();
		Command cmd = PatchSshCommand.createCreatePatchBranchCmd("someBranch" + RandomUtils.nextInt(), "HEAD", "-d /home/che/local/cvs ",
				Lists.newArrayList("module1", "module2"));
		cmd.noSystemCheck(true);
		runner.run(cmd);
		runner.postProcess();
	}
	
	@Ignore("TODO make preconditions = cvs setup automatic")
	@Test
	public void testCreatePatchTag() throws Exception {
		// JHE (25.06.2018): Within JAVA8MIG-386, we removed vcs.password, probably this test would noe fail		
		CommandRunner runner = new JschSessionCmdRunnerFactory("che","192.168.17.129").create();
		runner.preProcess();
		Command cmd = PatchSshCommand.createTagPatchModulesCmd("someTag" + RandomUtils.nextInt(), "HEAD", "-d /home/che/local/cvs ",
				Lists.newArrayList("module1", "module2"));
		cmd.noSystemCheck(true);
		runner.run(cmd);
		runner.postProcess();
	}
	
	@Ignore("TODO make preconditions = cvs setup automatic")
	@Test
	public void testDiff() throws Exception {
		// JHE (25.06.2018): Within JAVA8MIG-386, we removed vcs.password, probably this test would noe fail		
		CommandRunner runner = new JschSessionCmdRunnerFactory("che","192.168.17.129").create();
		runner.preProcess();
		Command cmd = PatchSshCommand.createDiffPatchModulesCmd("testBranch", "HEAD", "-d /home/che/local/cvs ",
				Lists.newArrayList("module1", "module2"));
		runner.run(cmd);
		cmd.noSystemCheck(true);
		runner.run(cmd);
		runner.postProcess();
	}

}
