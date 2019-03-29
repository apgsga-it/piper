package com.apgsga.microservice.mock.server;

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.ResourceLoader;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

@SpringBootApplication
@ComponentScan(basePackages = { "com.apgsga.microservice.patch.server" })
public class MockPatchServer {

	public static void main(String[] args) throws Exception {
		// TODO (che, 13.3.2019) : hack around around supressing logback,xml
		// from apg-patch-artifact-querymanager in classpath. Needs to be fixed there
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		loggerContext.reset();
		JoranConfigurator configurator = new JoranConfigurator();
		ResourceLoader rl = new FileSystemResourceLoader();
		configurator.setContext(loggerContext);
		configurator.doConfigure(rl.getResource("classpath:logback-mocktest.xml").getInputStream()); 
		SpringApplication springApplication = new SpringApplication(MockPatchServer.class);
		springApplication.setAdditionalProfiles("mock");
		springApplication.run(args);
	}
}
