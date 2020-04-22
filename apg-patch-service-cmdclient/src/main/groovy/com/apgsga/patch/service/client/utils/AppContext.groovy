package com.apgsga.patch.service.client.utils

import com.apgsga.patch.service.client.PliConfig
import org.springframework.context.annotation.AnnotationConfigApplicationContext

@Singleton()
public class AppContext {
	
	public def load() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(PliConfig.class);
		context.getBean(ConfigObject.class);
	}

}
