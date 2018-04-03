package com.apgsga.microservice.patch.server.impl.vcs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasypt.util.text.BasicTextEncryptor;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class JschSessionFactoryDefaultImpl implements JschSessionFactory {

	protected final Log LOGGER = LogFactory.getLog(getClass());

	private final String user;

	private final String password;

	private final String host;

	public JschSessionFactoryDefaultImpl(String user, String password, String host) {
		super();
		this.user = user;
		this.password = password;
		this.host = host;
	}

	@Override
	public VcsCommandSession create() {
		JSch jsch = new JSch();
		Session session;
		try {
			session = jsch.getSession(user, host, 22);
		} catch (JSchException e) {
			throw new RuntimeException(e);
		}
		session.setPassword(decrypt(password));
		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
		final VcsCommandSession jschSession = new JschCvsSession(session);
		return jschSession;
	}

	private String decrypt(String input) {
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword("test");
		return textEncryptor.decrypt(input);
	}

}
