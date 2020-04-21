package com.apgsga.microservice.patch.server.impl.vcs;

import java.util.List;

public interface VcsCommandRunner {
	
	public void preProcess(); 
	public List<String> run(VcsCommand cmd);
	public void postProcess(); 

}
