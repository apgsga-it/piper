package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;

import java.util.List;

@JsonDeserialize(builder = ServicesMetaData.ServicesMetaDataBuilder.class)
@Value
@Builder
public class ServicesMetaData  {
	
	private List<ServiceMetaData> servicesMetaData;

	@JsonPOJOBuilder(withPrefix = "")
	public static class ServicesMetaDataBuilder {}
}
