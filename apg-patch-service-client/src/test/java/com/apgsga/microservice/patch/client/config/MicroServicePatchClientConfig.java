package com.apgsga.microservice.patch.client.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class MicroServicePatchClientConfig {

	@Bean
	public MessageSource messageSource() {
		MessageSource messageSource = new ReloadableResourceBundleMessageSource();
		return messageSource;
	}

}
