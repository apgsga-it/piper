package com.apgsga.microservice.patch.core.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.apgsga.microservice.patch.exceptions.Asserts;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

public class GroovyScriptActionExecutor implements PatchActionExecutor {

	protected static final Log LOGGER = LogFactory.getLog(GroovyScriptActionExecutor.class.getName());

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
	
	

	public String getConfigFileName() {
		return configFileName;
	}

	public void setConfigFileName(String configFileName) {
		this.configFileName = configFileName;
	}

	@Override
	public void execute(String patchNumber, String toStatus) {
		Asserts.notNullOrEmpty(patchNumber, "GroovyScriptActionExecutor.execute.patchnumber.notnullorempty.assert",
				new Object[] {toStatus });
		Asserts.isTrue((patchContainer.getRepo().patchExists(patchNumber)),
				"GroovyScriptActionExecutor.execute.patch.exists.assert", new Object[] { patchNumber, toStatus });
		final Binding sharedData = new Binding();
		final GroovyShell shell = new GroovyShell(sharedData);
		sharedData.setProperty("configDir", configDir);
		sharedData.setProperty("patchNumber", patchNumber);
		sharedData.setProperty("configFileName", configFileName);
		sharedData.setProperty("toState", toStatus);
		sharedData.setProperty("patchContainerBean", patchContainer);
		ResourcePatternResolver rl = new PathMatchingResourcePatternResolver();
		try {
			Resource[] scriptResources = rl.getResources(groovyScriptFile);
			assert scriptResources.length == 1;
			InputStream scriptFile = scriptResources[0].getInputStream();
			String script = IOUtils.toString(scriptFile, "UTF-8");
			LOGGER.info("About to execute script:");
			LOGGER.info(script);
			LOGGER.info("With binding:" + sharedData.getVariables().toString());
			Object result = shell.evaluate(script);
			LOGGER.info("Result: " + (result == null ? " <Empty> " : result.toString()));
		} catch (CompilationFailedException | IOException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("GroovyScriptActionExecutor.execute.exception",
					new Object[] { e.getMessage(), patchNumber, toStatus, configDir, configFileName }, e);
		}
	}

}
