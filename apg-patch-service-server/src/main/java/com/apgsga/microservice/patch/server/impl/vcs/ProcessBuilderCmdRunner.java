package com.apgsga.microservice.patch.server.impl.vcs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.assertj.core.util.Lists;

public class ProcessBuilderCmdRunner implements VcsCommandRunner {

	public List<String> run(VcsCommand command) {
		ProcessBuilder pb = new ProcessBuilder().command(command.getCommand());
		Process p;
		try {
			p = pb.start();
			StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
			StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");
			outputGobbler.start();
			errorGobbler.start();
			int exit = p.waitFor();
			errorGobbler.join();
			outputGobbler.join();
			if (exit != 0 ) {
				throw new AssertionError(String.format("ProcessBuilder returned ExitCode %d", exit));
			}
			if (errorGobbler.getOutput().size() > 0) {
				throw new AssertionError("ProcessBuilder Erroroutput");
			}
			return Lists.newArrayList(outputGobbler.getOutput());
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private static class StreamGobbler extends Thread {

		List<String> output = Lists.newArrayList();
		InputStream is;
		String type;

		private StreamGobbler(InputStream is, String type) {
			this.is = is;
			this.type = type;
		}

		@Override
		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null) {
					System.out.println(type + "> " + line);
					output.add(line);
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
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
