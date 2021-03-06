package com.apgsga.microservice.patch.core.commands.patch.vcs;

import java.util.List;

public class DiffPatchModulesCmd extends PatchSshCommand {

	public DiffPatchModulesCmd(String patchBranch, String prodBranch, String additionalOptions,List<String> modules) {
		super(patchBranch, prodBranch, additionalOptions,modules);
	}


	@Override
	protected String[] getFirstPart() {
		return new String[] {  "-f", "rdiff", "-u", "-r", prodBranch, "-r", patchTag };
	}

}
