package com.apgsga.microservice.patch.core.commands;

import com.apgsga.microservice.patch.exceptions.Asserts;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;


public class ProcessBuilderCmdRunner implements CommandRunner {
	
	protected static final Log LOGGER = LogFactory.getLog(ProcessBuilderCmdRunner.class.getName());

	public List<String> run(Command command) {
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
			Asserts.isTrue(exit == 0 , "ProcessBuilder returned ExitCode: %d for Command: %s", exit, command.getCommand());
			return Lists.newArrayList(outputGobbler.getOutput());
		} catch (IOException | InterruptedException e) {
			throw ExceptionFactory.create("ProcessBuilder failed execute Command :  %s",e, (Object) command.getCommand());
		}
	}

	public static class StreamGobbler extends Thread {

		final List<String> output = Lists.newArrayList();
		final InputStream is;

		public StreamGobbler(InputStream is) {
			this.is = is;
		}

		@Override
		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line;
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

		public void log() {
			StringBuffer buffer = new StringBuffer();
			output.forEach((line) -> append(buffer,line));
            LOGGER.info(buffer.toString());
		}

		public static void append(StringBuffer buffer, String line) {
			buffer.append(line);
			buffer.append('\n');
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
