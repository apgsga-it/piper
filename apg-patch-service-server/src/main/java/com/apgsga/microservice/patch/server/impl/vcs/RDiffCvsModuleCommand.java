package com.apgsga.microservice.patch.server.impl.vcs;

import org.apache.commons.lang.SystemUtils;

public class RDiffCvsModuleCommand implements VcsCommand {
	
	private boolean noSystemCheck = false;
	
	private String cvsModule;
	
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
//		return new String[] {"ls", varLocalCvsRoot + getCvsModule()};
		// TODO JHE: get branch name as parameter
		return new String[] {"cvs", "rdiff", "-r", "it21_release_9_0_6_admin_uimig", "-r", "HEAD", getCvsModule() };
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

}
