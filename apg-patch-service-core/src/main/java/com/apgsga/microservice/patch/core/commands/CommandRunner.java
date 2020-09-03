package com.apgsga.microservice.patch.core.commands;

import java.util.List;

public interface CommandRunner {
	
	public void preProcess(); 
	public List<String> run(Command cmd);
	public void postProcess(); 

}
