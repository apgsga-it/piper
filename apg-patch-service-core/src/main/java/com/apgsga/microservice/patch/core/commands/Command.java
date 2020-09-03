package com.apgsga.microservice.patch.core.commands;

public interface Command {

	public String[] getCommand();
	
	public void noSystemCheck(boolean noChecnk); 

}
