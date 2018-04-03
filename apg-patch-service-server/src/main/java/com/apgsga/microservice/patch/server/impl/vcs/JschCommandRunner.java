package com.apgsga.microservice.patch.server.impl.vcs;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * @author che
 * @deprecated consider to use https://github.com/northern-bites/ganymed-ssh2 resp https://www.cleondris.com/opensource/ssh2/
 * Reason: Error Handling is not very good
 */
public class JschCommandRunner implements VcsCommandRunner {

	protected final Log LOGGER = LogFactory.getLog(getClass());

	private final Session session;

	public JschCommandRunner(Session session) {
		super();
		this.session = session;
	}

	public void preProcess() {
		try {
			session.connect();
		} catch (JSchException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public List<String> run(VcsCommand vcsCmd) {
		String command = String.join(" ", vcsCmd.getCommand());
		LOGGER.info("Executing: " + command);
		List<String> resultLines = Lists.newArrayList();
		try {
			ChannelExec channel = (ChannelExec) session.openChannel("exec");
			channel.setCommand(command);
			channel.setInputStream(null);
			channel.setErrStream(System.err);
			InputStream in = channel.getInputStream();
			channel.connect();
			while (true) {
				List<String> result = IOUtils.readLines(in);
				resultLines.addAll(result);
				if (channel.isClosed()) {
					if (in.available() > 0)
						continue;
					LOGGER.info("Ssh Channel Exitstatus: " + channel.getExitStatus());
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
				}
			}
			channel.disconnect();
		} catch (Exception e) {
			throw new RuntimeException("Error Executeing Jshell Command: " + command, e);
		}
		resultLines.stream().forEach(l -> LOGGER.info(l));
		LOGGER.info("Done: " + command);
		return resultLines;
	}

	public void postProcess() {
		session.disconnect();
	}
}