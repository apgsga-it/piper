package com.apgsga.microservice.patch.api;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;

import java.util.List;

@JsonDeserialize(builder = ServiceMetaData.ServiceMetaDataBuilder.class)
@Value
@Builder
public class ServiceMetaData {

	private String serviceName;
	private MavenArtifact bomCoordinates;
	private String microServiceBranch;
	private String baseVersionNumber;
	private String revisionMnemoPart;
	private List<Package> packages;

	@JsonPOJOBuilder(withPrefix = "")
	public static class ServiceMetaDataBuilder {}

}
