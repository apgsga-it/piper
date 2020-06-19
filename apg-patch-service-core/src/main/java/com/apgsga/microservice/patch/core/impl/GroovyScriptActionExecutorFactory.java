package com.apgsga.microservice.patch.core.impl;

public class GroovyScriptActionExecutorFactory implements PatchActionExecutorFactory {
	
	private String configDir;
	private String configFileName;
	private String groovyScriptFile;
	

	public GroovyScriptActionExecutorFactory(String configDir, String configFileName, String groovyScriptFile) {
		super();
		this.configDir = configDir;
		this.configFileName = configFileName;
		this.groovyScriptFile = groovyScriptFile;
	}



	@Override
	public PatchActionExecutor create(SimplePatchContainerBean patchContainer) {
		return new GroovyScriptActionExecutor(configDir, configFileName, groovyScriptFile, patchContainer); 
	}

}
