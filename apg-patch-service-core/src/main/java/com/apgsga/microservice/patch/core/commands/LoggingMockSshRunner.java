package com.apgsga.microservice.patch.core.commands;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

public class LoggingMockSshRunner implements CommandRunner {
	
	protected final Log LOGGER = LogFactory.getLog(getClass());

	@Override
	public void preProcess() {
		LOGGER.info("Mocking connect");
	}

	@Override
	public void postProcess() {
		LOGGER.info("Mocking disconnect");

	}

	@Override
	public List<String> run(Command sshCommand) {
		String command = String.join(" ", sshCommand.getCommand());
		LOGGER.info("Mocking execCommand with: " + command);
		LOGGER.info("Returning empty List");
		// TODO 1.5 (CHE , JHE) for one special cvs command, could break other tests
		return Lists.newArrayList("0");
	}

}
