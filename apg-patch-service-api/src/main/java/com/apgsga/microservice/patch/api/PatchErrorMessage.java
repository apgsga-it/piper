package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;

@JsonDeserialize(builder = PatchErrorMessage.PatchErrorMessageBuilder.class)
@Value
@Builder
public class PatchErrorMessage {

	private String timestamp;
	private String errorKey;
	private String errorText;
	private String causeExceptionMsg;
	private String stackTrace;

	@JsonPOJOBuilder(withPrefix = "")
	public static class PatchErrorMessageBuilder {}
}
