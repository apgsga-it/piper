package com.apgsga.microservice.patch.core.commands;

public interface Command {

	String[] getCommand();
	
	void noSystemCheck(boolean noChecnk);

}
