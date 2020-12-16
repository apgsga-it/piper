package com.apgsga.microservice.patch.exceptions;

public class ExceptionFactory {

	private ExceptionFactory() {
	}

	public static PatchServiceRuntimeException create(String errorMessage) {
		return new PatchServiceRuntimeException(errorMessage);
	}

	public static PatchServiceRuntimeException create(String messageTemplate, Throwable cause, Object... args) {
		return new PatchServiceRuntimeException(String.format(messageTemplate, args), cause);
	}

	public static PatchServiceRuntimeException create(String messageTemplate, Object... args) {
		return new PatchServiceRuntimeException( String.format(messageTemplate, args));
	}


}
