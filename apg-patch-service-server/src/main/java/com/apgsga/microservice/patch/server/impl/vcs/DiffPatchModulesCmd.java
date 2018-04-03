package com.apgsga.microservice.patch.server.impl.vcs;

public class DiffPatchModulesCmd extends CvsCmd {

	public DiffPatchModulesCmd(String patchBranch, String prodBranch, String... modules) {
		super(patchBranch, prodBranch, modules);
	}

	@Override
	protected String[] getFristPart() {
		return new String[] { "cvs", "-f", "rdiff", "-u", "-r", prodBranch, "-r", patchBranch };
	}

}
