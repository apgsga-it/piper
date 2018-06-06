package com.apgsga.microservice.patch.server;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.apgsga.microservice.patch.api.PatchErrorMessage;
import com.apgsga.microservice.patch.exceptions.PatchServiceRuntimeException;

@ControllerAdvice
public class PatchServiceExceptionHandler extends ResponseEntityExceptionHandler {

	protected final Log LOGGER = LogFactory.getLog(getClass());

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
		return error(ex, status); 
	}

	@ExceptionHandler(PatchServiceRuntimeException.class)
	public ResponseEntity<Object> patchServiceRuntimeException(final PatchServiceRuntimeException e) {
		return error(e);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Object> assertionException(final IllegalArgumentException e) {
		return error(e, HttpStatus.CONFLICT);
	}
	
	@ExceptionHandler({ Exception.class })
	public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
	   return error(ex,HttpStatus.INTERNAL_SERVER_ERROR); 
	}
	
	private ResponseEntity<Object> error(final PatchServiceRuntimeException exception) {
		return error(exception, exception.getHttpStatusCode()); 
	}

	private ResponseEntity<Object> error(final Exception exception, final HttpStatus httpStatus) {
		final String errorKey = exception.getClass().isAssignableFrom(PatchServiceRuntimeException.class) ? ((PatchServiceRuntimeException) exception).getMessageKey() : "";
		final String errorText = Optional.of(exception.getMessage()).orElse(exception.getClass().getSimpleName());
		Throwable cause = exception.getCause();
		final String causeMsg = cause != null ? cause.getMessage() : "<This Exception is Root Cause>";
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		PatchErrorMessage errorMsg = new PatchErrorMessage(timeStamp, errorKey,errorText, causeMsg,
				ExceptionUtils.getFullStackTrace(exception));
		LOGGER.warn(errorMsg.toString());
		return new ResponseEntity<>(errorMsg, httpStatus);
	}

}
