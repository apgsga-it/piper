package com.apgsga.microservice.patch.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@ComponentScan(basePackages = { "com.apgsga.microservice.patch.server", "com.apgsga.microservice.patch.core", "com.apgsga.system.mapping", "com.apgsga.patch.db.integration" })
@EnableWebMvc
public class MicroPatchServer {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(MicroPatchServer.class, args);
	}

}