package com.apgsga.microservice.patch.exceptions;

import java.util.Locale;

import org.springframework.context.MessageSource;

public class ExceptionUtils {

	public ExceptionUtils() { 
		super();
	}
	
	public static void throwPatchServiceException(String key,MessageSource messageSource, Object[] variables) throws PatchServiceRuntimeException
	{
		throw new PatchServiceRuntimeException(getErrorMsg(key, messageSource, variables)); 
	}
	
	public static void throwPatchServiceException(String key,MessageSource messageSource, Object[] variables, Throwable cause) throws PatchServiceRuntimeException {
		throw new PatchServiceRuntimeException(getErrorMsg(key, messageSource, variables), cause); 
	}
	private static String getErrorMsg(String key, MessageSource messageSource, Object[] variables) {
		String errorMsg = messageSource.getMessage(key,variables , Locale.getDefault());
		return errorMsg;
	}

}
