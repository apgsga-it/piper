package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;

import java.util.Date;

@JsonDeserialize(builder = PatchLogDetails.PatchLogDetailsBuilder.class)
@Value
@Builder
public class PatchLogDetails  {

	Date datetime;
	String target;
	String patchPipelineTask;
	String logText;

	@JsonPOJOBuilder(withPrefix = "")
	public static class PatchLogDetailsBuilder {}

}
