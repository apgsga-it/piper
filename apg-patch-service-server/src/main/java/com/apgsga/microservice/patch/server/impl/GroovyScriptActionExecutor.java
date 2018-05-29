package com.apgsga.microservice.patch.server.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.apgsga.microservice.patch.exceptions.PatchServiceRuntimeException;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

public class GroovyScriptActionExecutor implements PatchActionExecutor {

	protected final Log LOGGER = LogFactory.getLog(getClass());

	private String configDir;
	private String configFileName;
	private String groovyScriptFile;
	private SimplePatchContainerBean patchContainer;

	public GroovyScriptActionExecutor() {
		super();
	}

	public GroovyScriptActionExecutor(String configDir, String configFileName, String groovyScriptFile,
			SimplePatchContainerBean patchContainer) {
		super();
		this.configDir = configDir;
		this.configFileName = configFileName;
		this.groovyScriptFile = groovyScriptFile;
		this.patchContainer = patchContainer;
	}

	public String getConfigDir() {
		return configDir;
	}

	public void setConfigDir(String configDir) {
		this.configDir = configDir;
	}

	public String getGroovyScriptFile() {
		return groovyScriptFile;
	}

	public void setGroovyScriptFile(String groovyScriptFile) {
		this.groovyScriptFile = groovyScriptFile;
	}

	@Override
	public void execute(String patchNumber, String toStatus) {
		final Binding sharedData = new Binding();
		final GroovyShell shell = new GroovyShell(sharedData);
		sharedData.setProperty("configDir", configDir);
		sharedData.setProperty("patchNumber", patchNumber);
		sharedData.setProperty("configFileName", configFileName);
		sharedData.setProperty("toState", toStatus);
		sharedData.setProperty("patchContainerBean", patchContainer);
		ResourceLoader rl = new FileSystemResourceLoader();
		Resource scriptResource = rl.getResource(groovyScriptFile);
		try {
			File scriptFile = scriptResource.getFile();
			LOGGER.info(
					"About to execute script file: " + scriptFile.getAbsolutePath() + ", with toStatus: " + toStatus);
			String script = FileUtils.readFileToString(scriptFile, "UTF-8");
			LOGGER.info("About to execute script:");
			LOGGER.info(script);
			LOGGER.info("With binding:" + sharedData.getVariables().toString());
			Object result = shell.evaluate(script);
			LOGGER.info("Result: " + result == null ? " <Empty> " : result.toString());
		} catch (CompilationFailedException | IOException e) {
			throw new PatchServiceRuntimeException(
					"Execution of Groovy Action script " + groovyScriptFile + " failed for Patch : " + patchNumber
							+ ", toStatus: " + toStatus + ", configDir: " + configDir + " and File: " + configFileName,
					e);
		}
	}

}
