package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@JsonDeserialize(builder = PatchLog.PatchLogBuilder.class)
@Value
@Builder(toBuilder = true)
public class PatchLog {

	String patchNumber;
	List<PatchLogDetails> logDetails;

	@JsonPOJOBuilder(withPrefix = "")
	public static class PatchLogBuilder {}
}