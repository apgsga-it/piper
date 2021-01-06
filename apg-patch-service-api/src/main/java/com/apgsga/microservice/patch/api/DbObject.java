package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.io.FilenameUtils;

@JsonDeserialize(builder = DbObject.DbObjectBuilder.class)
@Value
@Builder
public class DbObject   {

	String fileName;
	String filePath;
	String moduleName;
	@lombok.Builder.Default
	transient Boolean hasConflict = false;

	@JsonPOJOBuilder(withPrefix = "")
	public static class DbObjectBuilder {}

	public String asFullPath() {
		String fullPath = getFilePath() + "/" + getFileName();
		return FilenameUtils.separatorsToUnix(fullPath);
	}
}
