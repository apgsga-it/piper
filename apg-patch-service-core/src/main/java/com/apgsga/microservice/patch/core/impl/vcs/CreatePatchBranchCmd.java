package com.apgsga.microservice.patch.core.impl.vcs;

import java.util.List;

public class CreatePatchBranchCmd extends PatchVcsCommand {

	public CreatePatchBranchCmd(String patchBranch, String prodBranch, List<String> modules) {
		super(patchBranch, prodBranch, modules);
	}

	public CreatePatchBranchCmd(String patchBranch, String prodBranch, String additionalOptions, List<String> modules) {
		super(patchBranch,prodBranch,additionalOptions,modules); 
	}

	@Override
	protected String[] getFristPart() {
		return new String[] { "rtag", "-b", "-r", prodBranch, patchTag };
	}

}
