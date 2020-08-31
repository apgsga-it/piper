package com.apgsga.microservice.patch.core.ssh;

import java.util.List;

import com.apgsga.microservice.patch.core.ssh.SshCommand;
import com.apgsga.microservice.patch.core.ssh.SshCommandRunner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

public class LoggingMockSshRunner implements SshCommandRunner {
	
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
	public List<String> run(SshCommand sshCommand) {
		String command = String.join(" ", sshCommand.getCommand());
		LOGGER.info("Mocking execCommand with: " + command);
		LOGGER.info("Returning empty List");
		// TODO 1.5 (CHE , JHE) for one special cvs command, could break other tests
		return Lists.newArrayList("0");
	}

}
