package com.apgsga.microservice.patch.server.impl.vcs;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

public class JschLoggingMockSession implements VcsCommandSession {
	
	protected final Log LOGGER = LogFactory.getLog(getClass());

	@Override
	public void connect() {
		LOGGER.info("Mocking connect");
	}

	@Override
	public void disconnect() {
		LOGGER.info("Mocking disconnect");

	}

	@Override
	public List<String> execCommand(String[] commands) {
		String command = String.join(" ", commands);
		LOGGER.info("Mocking execCommand with: " + command);
		LOGGER.info("Returning empty List");
		return Lists.newArrayList();
	}

}
