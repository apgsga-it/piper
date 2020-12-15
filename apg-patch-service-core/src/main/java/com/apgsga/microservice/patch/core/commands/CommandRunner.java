package com.apgsga.microservice.patch.core.commands;

import java.util.List;

public interface CommandRunner {
	
	void preProcess();
	List<String> run(Command cmd);
	void postProcess();

}
