package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;

@JsonDeserialize(builder = DbObject.DbObjectBuilder.class)
@Value
@Builder
public class DbObject   {

	private String fileName;
	private String filePath;
	private String moduleName;
	@lombok.Builder.Default
	private transient Boolean hasConflict = false;

	@JsonPOJOBuilder(withPrefix = "")
	static class DbObjectBuilder {}

}
