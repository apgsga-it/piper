package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.stream.Collectors;

@JsonDeserialize(builder = Service.ServiceBuilder.class)
@Value
@Builder(toBuilder = true)
public class Service {

    String serviceName;
    @Builder.Default
    List<MavenArtifact> artifactsToPatch = Lists.newArrayList();
    ServiceMetaData serviceMetaData;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ServiceBuilder {}

    public List<String> retrieveMavenArtifactsAsVcsPath() {
        return retrieveMavenArtifactsToBuild().stream().map(MavenArtifact::getName).collect(Collectors.toList());
    }


    public List<MavenArtifact> retrieveMavenArtifactsToBuild() {
        return artifactsToPatch.stream().filter(m -> m.getVersion().endsWith("SNAPSHOT")).collect(Collectors.toList());
    }
}
