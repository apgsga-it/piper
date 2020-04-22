package com.apgsga.microservice.patch.core.impl.vcs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProcessBuilderCmdRunnerFactory implements VcsCommandRunnerFactory {
	
	protected final Log LOGGER = LogFactory.getLog(getClass());


	@Override
	public VcsCommandRunner create() {
		LOGGER.info("Create ProcessBuilderRunner ");
		return new ProcessBuilderCmdRunner(); 
	}

}
