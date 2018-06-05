package com.apgsga.microservice.patch.exceptions;

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

}
