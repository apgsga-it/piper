package com.apgsga.microservice.patch.server.impl.vcs;

public interface VcsCommand {

	public String[] getCommand();
	
	public void noSystemCheck(boolean noChecnk); 

}
