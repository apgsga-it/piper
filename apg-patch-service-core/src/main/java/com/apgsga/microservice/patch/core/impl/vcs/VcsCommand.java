package com.apgsga.microservice.patch.core.impl.vcs;

public interface VcsCommand {

	public String[] getCommand();
	
	public void noSystemCheck(boolean noChecnk); 

}
