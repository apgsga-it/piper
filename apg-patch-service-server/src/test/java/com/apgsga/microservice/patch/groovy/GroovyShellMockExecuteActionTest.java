package com.apgsga.microservice.patch.groovy;

import org.junit.Test;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

public class GroovyShellMockExecuteActionTest {
	
	@Test
	public void testSimpleScript() throws Exception {
		final Binding sharedData = new Binding();
		final GroovyShell shell = new GroovyShell(sharedData);
		sharedData.setProperty("configDir","src/test/resources/json");
		sharedData.setProperty("patchNumber", "999999");
		sharedData.setProperty("toState","Some State");
		ResourceLoader rl = new FileSystemResourceLoader();
		Resource script = rl.getResource("src/test/groovy/mockExecutePatchAction.groovy"); 
		Object result = shell.evaluate(script.getFile());
		System.out.println(result.toString());
	}

}
