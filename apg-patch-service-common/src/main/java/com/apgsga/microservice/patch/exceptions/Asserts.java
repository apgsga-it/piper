package com.apgsga.microservice.patch.exceptions;

import com.google.common.base.Strings;

public class Asserts {

	private Asserts() {
	}

	public static void notNull(Object object, String key, Object[] variables) {
		if (object == null) {
			throw ExceptionFactory.createPatchServiceRuntimeException(key, variables);
		}
	}

	public static void isTrue(Boolean expression, String key, Object[] variables) {
		if (!expression) {
			throw ExceptionFactory.createPatchServiceRuntimeException(key, variables);
		}
	}
	
	public static void isFalse(Boolean expression, String key, Object[] variables) {
		if (expression) {
			throw ExceptionFactory.createPatchServiceRuntimeException(key, variables);
		}
	}

	public static void notNullOrEmpty(String string, String key, Object[] variables) {
		if (Strings.isNullOrEmpty(string == null ? null : string.trim())) {
			throw ExceptionFactory.createPatchServiceRuntimeException(key, variables); 
		}
	}

}
