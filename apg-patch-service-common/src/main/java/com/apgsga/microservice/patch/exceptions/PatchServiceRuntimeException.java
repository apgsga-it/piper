package com.apgsga.microservice.patch.exceptions;

import org.springframework.http.HttpStatus;

public class PatchServiceRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private HttpStatus httpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR;
	
	private final String messageKey; 

	public PatchServiceRuntimeException(String messageKey,String message, Throwable cause) {
		super(message, cause);
		this.messageKey = messageKey;
	}

	public PatchServiceRuntimeException(String messageKey, String message) {
		super(message);
		this.messageKey = messageKey;
	}
	
	public PatchServiceRuntimeException(String messageKey,HttpStatus httpStatusCode,String message, Throwable cause) {
		this(messageKey,message, cause);
		this.httpStatusCode = httpStatusCode;
	}

	public PatchServiceRuntimeException(String messageKey,HttpStatus httpStatusCode,String message) {
		this(messageKey,message);
		this.httpStatusCode = httpStatusCode;
	}
	

	public HttpStatus getHttpStatusCode() {
		return httpStatusCode;
	}

	public String getMessageKey() {
		return messageKey;
	}
	

}
