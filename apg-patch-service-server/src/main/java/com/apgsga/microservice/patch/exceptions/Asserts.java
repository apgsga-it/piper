package com.apgsga.microservice.patch.exceptions;

import org.springframework.util.Assert;

public class Asserts {

	private Asserts() {
	}
	
	public static void notNull(Object object, String key, Object[] variables) {
		Assert.notNull(object, ExceptionFactory.getErrorMsg(key, variables));
	}

	public static void isTrue(Boolean boolean1, String key, Object[] variables) {
		Assert.isTrue(boolean1, ExceptionFactory.getErrorMsg(key, variables));		
	}

}
