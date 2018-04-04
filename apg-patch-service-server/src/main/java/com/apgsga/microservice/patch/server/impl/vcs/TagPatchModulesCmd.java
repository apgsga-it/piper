package com.apgsga.microservice.patch.server.impl.vcs;

import java.util.List;

public class TagPatchModulesCmd extends PatchVcsCommand {

	public TagPatchModulesCmd(String patchBranch, String prodBranch, List<String> modules) {
		super(patchBranch, prodBranch, modules);

	}

	public TagPatchModulesCmd(String patchBranch, String prodBranch, String additionalOptions, List<String> modules) {
		super(patchBranch,prodBranch,additionalOptions,modules); 
	}

	@Override
	protected String[] getFristPart() {
		return new String[] {"rtag",  "-r", prodBranch, patchTag };
	}

}
