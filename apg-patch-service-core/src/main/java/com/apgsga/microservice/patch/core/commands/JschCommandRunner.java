package com.apgsga.microservice.patch.core.commands;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.google.common.collect.Lists;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * @author che
 */
public class JschCommandRunner implements CommandRunner {

	protected static final Log LOGGER = LogFactory.getLog(JschCommandRunner.class.getName());

	private final Session session;

	public JschCommandRunner(Session session) {
		super();
		this.session = session;
	}

	public void preProcess() {
		try {
			session.connect();
		} catch (JSchException e) {
			throw ExceptionFactory.create("Exception : <%s>  Jsch connecting to host: %s",e,
					 e.getMessage(), session.getHost());
		}
	}

	@SuppressWarnings({"unchecked", "BusyWait"})
	public List<String> run(Command vcsCmd) {
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
				} catch (Exception ignored) {
				}
			}
			// TODO (che, 31.5) : is this enough? or should we be more restrictive here? 
			if (channel.getExitStatus() > 0) {
				LOGGER.warn("Command : " + command + " returning with exit code: " + channel.getExitStatus());
			}
			channel.disconnect();
		} catch (Exception e) {
			throw ExceptionFactory.create("Exception : <%s>  running remote ssh command %s",e,
					 e.getMessage(), command);
		}
		resultLines.forEach(LOGGER::info);
		LOGGER.info("Done: " + command);
		return resultLines;
	}

	public void postProcess() {
		session.disconnect();
	}
}
