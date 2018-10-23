package com.apgsga.patch.service.client.utils

import groovy.json.JsonSlurper
import groovy.transform.Synchronized
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext

import com.apgsga.patch.service.client.PliConfig

@Singleton()
public class AppContext {
	
	public def load() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(PliConfig.class);
		context.getBean(ConfigObject.class);
	}

}
