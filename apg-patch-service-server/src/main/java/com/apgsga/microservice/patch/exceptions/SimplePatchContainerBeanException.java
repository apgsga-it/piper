package com.apgsga.microservice.patch.exceptions;

public class SimplePatchContainerBeanException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SimplePatchContainerBeanException(String message, Throwable cause) {
		super(message, cause);
	}

	public SimplePatchContainerBeanException(String message) {
		super(message);
	}


}
