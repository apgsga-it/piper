package com.apgsga.microservice.patch.core.ssh;

import java.util.List;

//TODO JHE (27.08.2020) : to be renamed without Ssh
public interface SshCommandRunner {
	
	public void preProcess(); 
	public List<String> run(SshCommand cmd);
	public void postProcess(); 

}
