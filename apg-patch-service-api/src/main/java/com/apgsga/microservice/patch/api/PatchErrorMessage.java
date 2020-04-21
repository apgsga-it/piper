package com.apgsga.microservice.patch.api;

public class PatchErrorMessage {

	private String timestamp;
	private String errorKey;
	private String errorText;
	private String causeExceptionMsg;
	private String stackTrace;

	public PatchErrorMessage() {
	}

	public PatchErrorMessage(String timeStamp, String errorKey, String errorText, String causeExceptionMsg,
			String stackTrace) {
		super();
		this.timestamp = timeStamp;
		this.errorKey = errorKey;
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

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getErrorKey() {
		return errorKey;
	}

	public void setErrorKey(String errorKey) {
		this.errorKey = errorKey;
	}

	@Override
	public String toString() {
		return "PatchErrorMessage [timestamp=" + timestamp + ", errorKey=" + errorKey + ", errorText=" + errorText
				+ ", causeExceptionMsg=" + causeExceptionMsg + ", stackTrace=" + stackTrace + "]";
	}

}
