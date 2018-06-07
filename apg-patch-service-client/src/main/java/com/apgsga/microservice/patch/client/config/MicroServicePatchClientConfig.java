package com.apgsga.microservice.patch.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import com.apgsga.microservice.patch.client.MicroservicePatchClient;

@Configuration
public class MicroServicePatchClientConfig {
	

	@Value("${baseUrl}")
	private String baseUrl;
	
	@Bean
	public MicroservicePatchClient microservicePatchClient() {
		return new MicroservicePatchClient(baseUrl);
	}
	
	@Bean
	public MessageSource messageSource() {
		MessageSource messageSource = new ReloadableResourceBundleMessageSource();
		return messageSource;
	}

}
