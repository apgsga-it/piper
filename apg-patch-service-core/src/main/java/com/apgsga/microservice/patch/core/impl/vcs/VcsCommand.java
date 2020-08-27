package com.apgsga.microservice.patch.core.impl.vcs;

public interface VcsCommand {

	String[] getCommand();
	
	void noSystemCheck(boolean noChecnk);


}
