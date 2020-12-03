package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.collect.Lists;
import lombok.*;

import java.util.List;

@JsonDeserialize(builder = DbModules.DbModulesBuilder.class)
@Value
@Builder
public class DbModules {

	@lombok.Builder.Default
	private List<String> dbModules = Lists.newArrayList();

	@JsonPOJOBuilder(withPrefix = "")
	static class DbModulesBuilder {}

}
