package com.apgsga.microservice.patch.exceptions;

public class ExceptionFactory {

	private ExceptionFactory() {
	}

	public static PatchServiceRuntimeException createPatchServiceRuntimeException(String key, Object[] variables) {
		return new PatchServiceRuntimeException(key,ErrorMessages.getErrorMsg(key, variables));
	}

	public static PatchServiceRuntimeException createPatchServiceRuntimeException(String key, Object[] variables,
			Throwable cause) {
		return new PatchServiceRuntimeException(key,ErrorMessages.getErrorMsg(key, variables), cause);
	}

}
