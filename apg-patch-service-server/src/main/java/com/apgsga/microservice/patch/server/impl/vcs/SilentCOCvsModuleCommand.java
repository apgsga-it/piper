package com.apgsga.microservice.patch.server.impl.vcs;

import org.apache.commons.lang.SystemUtils;

public class SilentCOCvsModuleCommand implements VcsCommand {
	
	private boolean noSystemCheck = false;
	
	private String cvsModule;
	
	private String cvsBranch;
	
	@Override
	public String[] getCommand() {
		String[] processBuilderParm;
		if (SystemUtils.IS_OS_WINDOWS && !noSystemCheck) {
			processBuilderParm = new String[] { "bash.exe", "-c", "-s", getBashCommandAsSpaceSeperated() };
		} else {
			processBuilderParm = getBashCommand();
		}
		return processBuilderParm; 
	}

	private String getBashCommandAsSpaceSeperated() {
		String[] processParm = getBashCommand();
		String parameter = String.join(" ", processParm);
		return parameter;
	}
	
	protected String[] getBashCommand() {
		if(getCvsModule() == null || getCvsModule().isEmpty()) {
			throw new RuntimeException(this.getClass().getName() + ": cvsModule has to be set!");
		}
		if(getCvsBranch() == null || getCvsBranch().isEmpty()) {
			throw new RuntimeException(this.getClass().getName() + ": cvsBranch has to be set!");
		}
		return new String[] {"cvs", "co", "-p", "-r", getCvsBranch(), getCvsModule(), "&>/dev/null ; echo $?"};
	}

	@Override
	public void noSystemCheck(boolean noChecnk) {
		this.noSystemCheck = noChecnk;
	}

	public String getCvsModule() {
		return cvsModule;
	}

	public void setCvsModule(String cvsModule) {
		this.cvsModule = cvsModule;
	}

	public String getCvsBranch() {
		return cvsBranch;
	}

	public void setCvsBranch(String cvsBranch) {
		this.cvsBranch = cvsBranch;
	}

}
