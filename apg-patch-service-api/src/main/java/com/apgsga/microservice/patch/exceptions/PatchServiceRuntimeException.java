package com.apgsga.microservice.patch.exceptions;

public class PatchServiceRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;


	public PatchServiceRuntimeException(String message) {
		super(message);
	}

	public PatchServiceRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}


}
