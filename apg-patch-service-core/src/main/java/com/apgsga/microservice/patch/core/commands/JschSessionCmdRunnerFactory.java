package com.apgsga.microservice.patch.core.commands;

import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JschSessionCmdRunnerFactory implements CommandRunnerFactory {

	protected final Log LOGGER = LogFactory.getLog(getClass());

	private final String user;

	private final String host;

	public JschSessionCmdRunnerFactory(String user, String host) {
		super();
		this.user = user;
		this.host = host;
	}

	@Override
	public CommandRunner create() {
		JSch jsch = new JSch();
		Session session;
		try {
			session = jsch.getSession(user, host, 22);
			LOGGER.info("Got ssh Session on: " + host + " for: " + user);
		} catch (JSchException e) {
			throw ExceptionFactory.create("Ssh connection could not be establish with %s and host %s",e, user, host);
		}
		try {
			jsch.addIdentity("~/.ssh/id_rsa");
		} catch (JSchException e) {
			throw ExceptionFactory.create("Local Ssh public key file id_rsa could not be read",e);
		}
		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
		return new JschCommandRunner(session);
	}

}
