package com.apgsga.microservice.patch.api;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public class PatchErrorMessage {

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
	private LocalDateTime timestamp;
	private String errorText;
	private String causeExceptionMsg;
	private String stackTrace;

	public PatchErrorMessage() {
		timestamp = LocalDateTime.now();
	}

	public PatchErrorMessage(String errorText, String causeExceptionMsg, String stackTrace) {
		super();
		this.errorText = errorText;
		this.causeExceptionMsg = causeExceptionMsg;
		this.stackTrace = stackTrace;
	}

	public String getErrorText() {
		return errorText;
	}

	public void setErrorText(String errorText) {
		this.errorText = errorText;
	}

	public String getCauseExceptionMsg() {
		return causeExceptionMsg;
	}

	public void setCauseExceptionMsg(String causeExceptionMsg) {
		this.causeExceptionMsg = causeExceptionMsg;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

}
