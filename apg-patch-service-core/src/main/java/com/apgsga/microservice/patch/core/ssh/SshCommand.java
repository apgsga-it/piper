package com.apgsga.microservice.patch.core.ssh;

//TODO JHE (27.08.2020) : to be renamed without Ssh
public interface SshCommand {

	public String[] getCommand();
	
	public void noSystemCheck(boolean noChecnk); 

}
