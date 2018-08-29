package com.apgsga.microservice.patch.server.impl.vcs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;


public class ProcessBuilderCmdRunner implements VcsCommandRunner {
	
	protected static final Log LOGGER = LogFactory.getLog(ProcessBuilderCmdRunner.class.getName());

	public List<String> run(VcsCommand command) {
		ProcessBuilder pb = new ProcessBuilder().command(command.getCommand());
		Process p;
		try {
			p = pb.start();
			StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream());
			StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream());
			outputGobbler.start();
			errorGobbler.start();
			int exit = p.waitFor();
			errorGobbler.join();
			outputGobbler.join();
			if (exit != 0 ) {
				throw new AssertionError(String.format("ProcessBuilder returned ExitCode %d", exit));
			}
			return Lists.newArrayList(outputGobbler.getOutput());
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private static class StreamGobbler extends Thread {

		List<String> output = Lists.newArrayList();
		InputStream is;

		private StreamGobbler(InputStream is) {
			this.is = is;
		}

		@Override
		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null) {
					LOGGER.info(line);
					output.add(line);
				}
			} catch (IOException ioe) {
				LOGGER.error(ioe);
			}
		}
		public List<String> getOutput() {
			return output; 
		}
	}

	@Override
	public void preProcess() {
		// Do nothing
	}

	@Override
	public void postProcess() {
		// Do nothing

	}
}
