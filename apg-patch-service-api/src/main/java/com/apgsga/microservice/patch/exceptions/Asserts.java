package com.apgsga.microservice.patch.exceptions;

import com.google.common.base.Strings;

public class Asserts {

	private Asserts() {
	}

	public static void notNull(Object object, String messageTemplate, Object ...args) {
		if (object == null) {
			throw ExceptionFactory.create(messageTemplate, args);
		}
	}

	public static void isTrue(Boolean expression, String messageTemplate, Object ...args) {
		if (!expression) {
			throw ExceptionFactory.create(messageTemplate, args);
		}
	}


	public static void notNullOrEmpty(String string, String messageTemplate, Object ...args) {
		if (Strings.isNullOrEmpty(string == null ? null : string.trim())) {
			throw ExceptionFactory.create(messageTemplate, args);
		}
	}

}
