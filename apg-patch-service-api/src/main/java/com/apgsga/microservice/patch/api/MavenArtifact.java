package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;

@JsonDeserialize(builder = MavenArtifact.MavenArtifactBuilder.class)
@Data
@Builder
public class MavenArtifact {

	@NonNull
	private String artifactId;
	@NonNull
	private String groupId;
	private String name;
	private String version;
	@lombok.Builder.Default
	private Integer dependencyLevel = 0;

	public void augmentDependencyLevel() {
		dependencyLevel  = dependencyLevel + 1;
	}

	@JsonPOJOBuilder(withPrefix = "")
	public static class MavenArtifactBuilder {}
	

}
