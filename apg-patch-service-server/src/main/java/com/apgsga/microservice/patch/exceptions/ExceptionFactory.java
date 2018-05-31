package com.apgsga.microservice.patch.exceptions;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;

public class ExceptionFactory {
	
	private static ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();

	
	static {
		ResourceLoader rl = new FileSystemResourceLoader();
		Resource resource = rl.getResource("classpath:messages.properties");
		Properties properties = new Properties();
		try {
			properties.load(resource.getInputStream());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		messageSource.setCommonMessages(properties);
	}

	private ExceptionFactory() {
	}

	public static PatchServiceRuntimeException createPatchServiceRuntimeException(String key, Object[] variables)
			throws PatchServiceRuntimeException {
		return new PatchServiceRuntimeException(getErrorMsg(key, variables));
	}

	public static PatchServiceRuntimeException createPatchServiceRuntimeException(String key, Object[] variables,
			Throwable cause) throws PatchServiceRuntimeException {
		return new PatchServiceRuntimeException(getErrorMsg(key, variables), cause);
	}

	public static String getErrorMsg(String key, Object[] variables) {
		String errorMsg = messageSource.getMessage(key, variables, Locale.getDefault());
		return errorMsg;
	}

	public static void AssertNotNull(Object object, String key, Object[] variables) {
		Assert.notNull(object, getErrorMsg(key, variables));
	}

	public static void AssertTrue(Boolean boolean1, String key, Object[] variables) {
		Assert.isTrue(boolean1, getErrorMsg(key, variables));
	}

}
