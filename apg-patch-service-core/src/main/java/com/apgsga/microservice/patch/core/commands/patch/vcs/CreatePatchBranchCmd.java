package com.apgsga.microservice.patch.core.commands.patch.vcs;

import java.util.List;

public class CreatePatchBranchCmd extends PatchSshCommand {

	public CreatePatchBranchCmd(String patchBranch, String prodBranch, List<String> modules) {
		super(patchBranch, prodBranch, modules);
	}

	public CreatePatchBranchCmd(String patchBranch, String prodBranch, String additionalOptions, List<String> modules) {
		super(patchBranch,prodBranch,additionalOptions,modules); 
	}

	@Override
	protected String[] getFirstPart() {
		return new String[] { "rtag", "-b", "-r", prodBranch, patchTag };
	}

}
