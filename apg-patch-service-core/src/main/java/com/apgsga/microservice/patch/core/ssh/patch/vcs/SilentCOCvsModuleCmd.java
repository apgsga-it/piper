package com.apgsga.microservice.patch.core.ssh.patch.vcs;

import java.util.List;

public class SilentCOCvsModuleCmd extends PatchSshCommand {
	
	public SilentCOCvsModuleCmd(String cvsBranch, List<String> modules, String lastPart) {
		super(cvsBranch, modules, lastPart);
		// JHE: Forcing to true because this one could only be correctly tested with remote CVS option.
		noSystemCheck = true;
	}

	@Override
	protected String[] getFirstPart() {
		return new String[] {"co", "-p", "-r", prodBranch};
	}
}