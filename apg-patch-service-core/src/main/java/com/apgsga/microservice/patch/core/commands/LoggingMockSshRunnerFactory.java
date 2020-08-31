package com.apgsga.microservice.patch.core.commands;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LoggingMockSshRunnerFactory implements CommandRunnerFactory {
	
	protected final Log LOGGER = LogFactory.getLog(getClass());


	@Override
	public CommandRunner create() {
		LOGGER.info("Create Mock Session");
		return new LoggingMockSshRunner();
	}

}
