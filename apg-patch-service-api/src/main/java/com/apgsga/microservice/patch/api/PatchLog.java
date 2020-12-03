package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;

import java.util.List;

@JsonDeserialize(builder = PatchLog.PatchLogBuilder.class)
@Value
@Builder
public class PatchLog {

	private String patchNumber;
	private List<PatchLogDetails> logDetails;

	@JsonPOJOBuilder(withPrefix = "")
	public static class PatchLogBuilder {}
}