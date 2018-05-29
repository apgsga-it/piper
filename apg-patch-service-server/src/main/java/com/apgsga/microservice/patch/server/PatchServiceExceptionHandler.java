package com.apgsga.microservice.patch.server;

import java.util.Optional;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.apgsga.microservice.patch.api.PatchErrorMessage;
import com.apgsga.microservice.patch.exceptions.PatchServiceRuntimeException;

public class PatchServiceExceptionHandler extends ResponseEntityExceptionHandler {

	protected final Log LOGGER = LogFactory.getLog(getClass());

	@ExceptionHandler(PatchServiceRuntimeException.class)
	public ResponseEntity<PatchErrorMessage> notFoundException(final PatchServiceRuntimeException e) {
		return error(e);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<PatchErrorMessage> assertionException(final IllegalArgumentException e) {
		return error(e, HttpStatus.CONFLICT);
	}
	
	private ResponseEntity<PatchErrorMessage> error(final PatchServiceRuntimeException exception) {
		return error(exception, exception.getHttpStatusCode()); 
	}

	// TODO (che,23.5) : First go, to be discussed
	private ResponseEntity<PatchErrorMessage> error(final Exception exception, final HttpStatus httpStatus) {
		final String errorText = Optional.of(exception.getMessage()).orElse(exception.getClass().getSimpleName());
		Throwable cause = exception.getCause();
		final String causeMsg = cause != null ? cause.getMessage() : "<Root Cause>";
		PatchErrorMessage errorMsg = new PatchErrorMessage(errorText, causeMsg,
				ExceptionUtils.getFullStackTrace(exception));
		LOGGER.warn(errorMsg.toString());
		return new ResponseEntity<>(errorMsg, httpStatus);
	}

}
