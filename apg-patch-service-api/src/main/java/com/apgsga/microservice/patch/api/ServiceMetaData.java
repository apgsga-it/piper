package com.apgsga.microservice.patch.api;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@JsonDeserialize(builder = ServiceMetaData.ServiceMetaDataBuilder.class)
@Value
@Builder
public class ServiceMetaData {

	String serviceName;
	String revisionPkgName;
	MavenArtifact bomCoordinates;
	String microServiceBranch;
	String baseVersionNumber;
	String revisionMnemoPart;
	List<Package> packages;

	@JsonPOJOBuilder(withPrefix = "")
	public static class ServiceMetaDataBuilder {}

}
