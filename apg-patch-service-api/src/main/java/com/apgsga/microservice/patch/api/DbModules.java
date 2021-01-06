package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@JsonDeserialize(builder = DbModules.DbModulesBuilder.class)
@Value
@Builder
public class DbModules {

	@lombok.Builder.Default
	List<String> dbModules = Lists.newArrayList();

	@JsonPOJOBuilder(withPrefix = "")
	public static class DbModulesBuilder {}

}
