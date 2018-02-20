package com.apgsga.microservice.patch.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.apgsga.microservice.patch.server" })

public class MicroPatchServer {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(MicroPatchServer.class, args);
	}

}