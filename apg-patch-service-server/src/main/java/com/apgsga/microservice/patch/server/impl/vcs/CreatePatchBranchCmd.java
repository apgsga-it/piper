package com.apgsga.microservice.patch.server.impl.vcs;

public class CreatePatchBranchCmd extends CvsCmd {

	public CreatePatchBranchCmd(String patchBranch, String prodBranch, String... modules) {
		super(patchBranch, prodBranch, modules);
	}

	@Override
	protected String[] getFristPart() {
		return new String[] { "cvs", "rtag", "-b", "-r", prodBranch, patchBranch };
	}

}
