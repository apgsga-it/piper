package com.apgsga.microservice.patch.core.ssh;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProcessBuilderCmdRunnerFactory implements SshCommandRunnerFactory {
	
	protected final Log LOGGER = LogFactory.getLog(getClass());


	@Override
	public SshCommandRunner create() {
		LOGGER.info("Create ProcessBuilderRunner ");
		return new ProcessBuilderCmdRunner();
	}

}
