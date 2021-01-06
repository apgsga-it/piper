package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@JsonDeserialize(builder = PatchErrorMessage.PatchErrorMessageBuilder.class)
@Value
@Builder
public class PatchErrorMessage {

	String timestamp;
	String errorText;
	String causeExceptionMsg;
	String stackTrace;

	@JsonPOJOBuilder(withPrefix = "")
	public static class PatchErrorMessageBuilder {}
}
