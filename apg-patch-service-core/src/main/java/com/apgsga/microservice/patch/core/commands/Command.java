package com.apgsga.microservice.patch.core.commands;

//TODO JHE (27.08.2020) : to be renamed without Ssh
public interface Command {

	public String[] getCommand();
	
	public void noSystemCheck(boolean noChecnk); 

}
