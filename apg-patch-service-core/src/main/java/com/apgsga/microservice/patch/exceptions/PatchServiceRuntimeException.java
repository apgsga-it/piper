package com.apgsga.microservice.patch.exceptions;

import org.springframework.http.HttpStatus;

public class PatchServiceRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final String messageKey;


	public PatchServiceRuntimeException(String message) {
		super(message);
		this.messageKey = "";
	}

	public PatchServiceRuntimeException(String messageKey,String message, Throwable cause) {
		super(message, cause);
		this.messageKey = messageKey;
	}

	public PatchServiceRuntimeException(String messageKey, String message) {
		super(message);
		this.messageKey = messageKey;
	}

	

	public String getMessageKey() {
		return messageKey;
	}
	

}
