package com.apgsga.microservice.patch.server.impl.vcs;

import java.util.List;

public class CreatePatchBranchCmd extends PatchVcsCommand {

	public CreatePatchBranchCmd(String patchBranch, String prodBranch, List<String> modules) {
		super(patchBranch, prodBranch, modules);
	}

	@Override
	protected String[] getFristPart() {
		return new String[] { "cvs", "rtag", "-b", "-r", prodBranch, patchBranch };
	}

}
