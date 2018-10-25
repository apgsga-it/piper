package com.apgsga.microservice.patch.server.impl.vcs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class JschSessionCmdRunnerFactory implements VcsCommandRunnerFactory {

	protected final Log LOGGER = LogFactory.getLog(getClass());

	private final String user;

	private final String host;

	public JschSessionCmdRunnerFactory(String user, String host) {
		super();
		this.user = user;
		this.host = host;
	}

	@Override
	public VcsCommandRunner create() {
		JSch jsch = new JSch();
		Session session;
		try {
			session = jsch.getSession(user, host, 22);
			LOGGER.info("Got ssh Session on: " + host + " for: " + user);
		} catch (JSchException e) {
			throw new RuntimeException(e);
		}
		try {
			jsch.addIdentity("~/.ssh/id_rsa");
		} catch (JSchException e) {
			throw new RuntimeException(e);
		}
		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
		final VcsCommandRunner jschSession = new JschCommandRunner(session);
		return jschSession;
	}

}
