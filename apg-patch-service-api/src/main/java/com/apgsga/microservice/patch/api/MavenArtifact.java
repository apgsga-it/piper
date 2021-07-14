package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@JsonDeserialize(builder = MavenArtifact.MavenArtifactBuilder.class)
@Builder(toBuilder = true)
@EqualsAndHashCode(exclude = {"name", "version", "dependencyLevel"})
public class MavenArtifact {

    @Getter
    final String artifactId;
    @Getter
    final String groupId;
    @Getter
    final String type;
    @Getter
    final String scope;
    @Getter
    String name;
    @Getter
    String version;
    @SuppressWarnings("UnusedAssignment")
    @Getter
    @lombok.Builder.Default
    Integer dependencyLevel = 0;

    public void augmentDependencyLevel() {
        dependencyLevel++;
    }

    public void withName(String name) {
        this.name = name;
    }

    public void withVersion(String version) {
        this.version = version;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class MavenArtifactBuilder {
    }


}
