package com.apgsga.microservice.patch.core.impl;

import com.apgsga.microservice.patch.exceptions.Asserts;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.apgsga.system.mapping.api.TargetSystemMapping;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;

public class GroovyScriptActionExecutor implements PatchActionExecutor {

	protected static final Log LOGGER = LogFactory.getLog(GroovyScriptActionExecutor.class.getName());

	private TargetSystemMapping tsm;
	private String groovyScriptFile;
	private SimplePatchContainerBean patchContainer;

	public GroovyScriptActionExecutor() {
		super();
	}

	public GroovyScriptActionExecutor(TargetSystemMapping tsm, String groovyScriptFile,
			SimplePatchContainerBean patchContainer) {
		super();
		this.tsm = tsm;
		this.groovyScriptFile = groovyScriptFile;
		this.patchContainer = patchContainer;
	}

	public String getGroovyScriptFile() {
		return groovyScriptFile;
	}

	public void setGroovyScriptFile(String groovyScriptFile) {
		this.groovyScriptFile = groovyScriptFile;
	}

	@Override
	public void execute(String patchNumber, String toStatus) {
		Asserts.notNullOrEmpty(patchNumber, "GroovyScriptActionExecutor.execute.patchnumber.notnullorempty.assert",
				new Object[] {toStatus });
		Asserts.isTrue((patchContainer.getRepo().patchExists(patchNumber)),
				"GroovyScriptActionExecutor.execute.patch.exists.assert", new Object[] { patchNumber, toStatus });
		final Binding sharedData = new Binding();
		final GroovyShell shell = new GroovyShell(sharedData);
		sharedData.setProperty("targetSystemMapping",tsm);
		sharedData.setProperty("patchNumber", patchNumber);
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
					new Object[] { e.getMessage(), patchNumber, toStatus}, e);
		}
	}

}
