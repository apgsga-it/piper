package com.apgsga.microservice.patch.core.impl;

import com.apgsga.system.mapping.api.TargetSystemMapping;

public class GroovyScriptActionExecutorFactory implements PatchActionExecutorFactory {
	
	private String groovyScriptFile;
	private TargetSystemMapping tsm;
	

	public GroovyScriptActionExecutorFactory(TargetSystemMapping tsm, String groovyScriptFile) {
		super();
		this.tsm = tsm;
		this.groovyScriptFile = groovyScriptFile;
	}

	@Override
	public PatchActionExecutor create(SimplePatchContainerBean patchContainer) {
		return new GroovyScriptActionExecutor(tsm, groovyScriptFile, patchContainer);
	}

}
