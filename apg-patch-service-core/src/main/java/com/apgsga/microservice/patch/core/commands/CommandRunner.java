package com.apgsga.microservice.patch.core.commands;

import java.util.List;

//TODO JHE (27.08.2020) : to be renamed without Ssh
public interface CommandRunner {
	
	public void preProcess(); 
	public List<String> run(Command cmd);
	public void postProcess(); 

}
