package com.apgsga.microservice.patch.server.impl.vcs;

import java.util.List;

public class SilentCOCvsModuleCmd extends PatchVcsCommand {
	
	public SilentCOCvsModuleCmd(String cvsBranch, List<String> modules, String lastPart) {
		super(cvsBranch, modules, lastPart);
		noSystemCheck = true;
	}

	@Override
	protected String[] getFristPart() {
		return new String[] {"co", "-p", "-r", prodBranch};
	}
}