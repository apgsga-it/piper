package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;

import java.util.Date;

@JsonDeserialize(builder = PatchLogDetails.PatchLogDetailsBuilder.class)
@Value
@Builder
public class PatchLogDetails  {

	private Date datetime;
	private String target;
	private String patchPipelineTask;
	private String logText;

	@JsonPOJOBuilder(withPrefix = "")
	public static class PatchLogDetailsBuilder {}

}
