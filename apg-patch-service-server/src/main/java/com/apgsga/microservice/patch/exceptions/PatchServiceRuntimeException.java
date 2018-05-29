package com.apgsga.microservice.patch.exceptions;

import org.springframework.http.HttpStatus;

public class PatchServiceRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private HttpStatus httpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR;

	public PatchServiceRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public PatchServiceRuntimeException(String message) {
		super(message);
	}
	
	public PatchServiceRuntimeException(HttpStatus httpStatusCode,String message, Throwable cause) {
		this(message, cause);
		this.httpStatusCode = httpStatusCode;
	}

	public PatchServiceRuntimeException(HttpStatus httpStatusCode,String message) {
		this(message);
		this.httpStatusCode = httpStatusCode;
	}
	

	public HttpStatus getHttpStatusCode() {
		return httpStatusCode;
	}
	

}
