package com.apgsga.microservice.patch.exceptions;

public class MessageFileLoadingException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MessageFileLoadingException() {
		super();
	}

	public MessageFileLoadingException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public MessageFileLoadingException(String message, Throwable cause) {
		super(message, cause);
	}

	public MessageFileLoadingException(String message) {
		super(message);
	}

	public MessageFileLoadingException(Throwable cause) {
		super(cause);
	}
	
	

}
