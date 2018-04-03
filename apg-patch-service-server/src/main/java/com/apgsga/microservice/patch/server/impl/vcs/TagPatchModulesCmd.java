package com.apgsga.microservice.patch.server.impl.vcs;

import java.util.List;

public class TagPatchModulesCmd extends PatchVcsCommand {



	public TagPatchModulesCmd(String patchBranch, String prodBranch, List<String> modules) {
		super(patchBranch, prodBranch, modules);

	}

	@Override
	protected String[] getFristPart() {
		return new String[] { "cvs", "-f", "rdiff", "-u", "-r", prodBranch, "-r", patchBranch };
	}

}
