package com.apgsga.microservice.patch.core.ssh.patch.vcs;

import java.util.List;

public class TagPatchModulesCmd extends PatchSshCommand {

	public TagPatchModulesCmd(String patchBranch, String prodBranch, List<String> modules) {
		super(patchBranch, prodBranch, modules);

	}

	public TagPatchModulesCmd(String patchBranch, String prodBranch, String additionalOptions, List<String> modules) {
		super(patchBranch,prodBranch,additionalOptions,modules); 
	}

	@Override
	protected String[] getFirstPart() {
		return new String[] {"rtag",  "-r", prodBranch, patchTag };
	}

}
