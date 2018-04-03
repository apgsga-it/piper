package com.apgsga.microservice.patch.server.impl.vcs;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

public class LoggingMockVcsRunner implements VcsCommandRunner {
	
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
	public List<String> run(VcsCommand vcsCommand) {
		String command = String.join(" ", vcsCommand.getCommand());
		LOGGER.info("Mocking execCommand with: " + command);
		LOGGER.info("Returning empty List");
		return Lists.newArrayList();
	}

}
