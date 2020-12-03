package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.collect.Lists;
import lombok.*;

import java.util.List;

@JsonDeserialize(builder = Service.ServiceBuilder.class)
@Value
@Builder
public class Service {

    String serviceName;
    @Builder.Default
    List<MavenArtifact> artifactsToPatch = Lists.newArrayList();

    @JsonPOJOBuilder(withPrefix = "")
    public static class ServiceBuilder {}
}
