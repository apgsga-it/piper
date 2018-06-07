package com.apgsga.microservice.patch.exceptions;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class ErrorMessages {

	private static final String NO_SUCH_MESSAGE_ERROR_MSG = "No Message found for key #key# with Variables: ";

	private static final String SPACE = " ";

	protected static final Log LOGGER = LogFactory.getLog(ErrorMessages.class.getName());

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

	public static String getErrorMsg(String key, Object[] variables) {
		String errorMsg = "";
		try {
			errorMsg = messageSource.getMessage(key, variables, Locale.getDefault());
		} catch (NoSuchMessageException e) {
			final StringBuffer bf = new StringBuffer();
			bf.append(NO_SUCH_MESSAGE_ERROR_MSG.replace("#key#", key));
			for (int i = 0; i < variables.length; i++) {
				bf.append(variables[i].toString());
				bf.append(SPACE);
			}
			errorMsg = bf.toString();
			LOGGER.warn(errorMsg);
		}
		return errorMsg;
	}

}
