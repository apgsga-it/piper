package com.apgsga.microservice.patch.server.impl.vcs;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;

public class IntegrationTestVcsSshCmds {

	@Ignore("TODO make preconditions = cvs setup automatic")
	@Test
	public void testCreatePatchBranch() throws Exception {
		// JHE (25.06.2018): Within JAVA8MIG-386, we removed vcs.password, probably this test would noe fail
		VcsCommandRunner runner = new JschSessionCmdRunnerFactory("che","192.168.17.129").create();
		runner.preProcess();
		VcsCommand cmd = PatchVcsCommand.createCreatePatchBranchCmd("someBranch" + RandomUtils.nextInt(), "HEAD", "-d /home/che/local/cvs ",
				Lists.newArrayList("module1", "module2"));
		cmd.noSystemCheck(true);
		runner.run(cmd);
		runner.postProcess();
	}
	
	@Ignore("TODO make preconditions = cvs setup automatic")
	@Test
	public void testCreatePatchTag() throws Exception {
		// JHE (25.06.2018): Within JAVA8MIG-386, we removed vcs.password, probably this test would noe fail		
		VcsCommandRunner runner = new JschSessionCmdRunnerFactory("che","192.168.17.129").create();
		runner.preProcess();
		VcsCommand cmd = PatchVcsCommand.createTagPatchModulesCmd("someTag" + RandomUtils.nextInt(), "HEAD", "-d /home/che/local/cvs ",
				Lists.newArrayList("module1", "module2"));
		cmd.noSystemCheck(true);
		runner.run(cmd);
		runner.postProcess();
	}
	
	@Ignore("TODO make preconditions = cvs setup automatic")
	@Test
	public void testDiff() throws Exception {
		// JHE (25.06.2018): Within JAVA8MIG-386, we removed vcs.password, probably this test would noe fail		
		VcsCommandRunner runner = new JschSessionCmdRunnerFactory("che","192.168.17.129").create();
		runner.preProcess();
		VcsCommand cmd = PatchVcsCommand.createDiffPatchModulesCmd("testBranch", "HEAD", "-d /home/che/local/cvs ",
				Lists.newArrayList("module1", "module2"));
		runner.run(cmd);
		cmd.noSystemCheck(true);
		runner.run(cmd);
		runner.postProcess();
	}

}
