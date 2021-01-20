package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

import java.util.List;
import java.util.stream.Collectors;

@JsonDeserialize(builder = Service.ServiceBuilder.class)
@EqualsAndHashCode(exclude = {"serviceMetaData", "patchTag"})
@Builder(toBuilder = true)
public class Service {

    @Getter
    String serviceName;
    @Builder.Default
    @Getter
    List<MavenArtifact> artifactsToPatch = Lists.newArrayList();
    @Getter
    String patchTag;
    @Getter
    ServiceMetaData serviceMetaData;

    public void withPatchTag(Integer tagNr, String patchNr) {
        this.patchTag = serviceMetaData.getMicroServiceBranch() + "_" +  patchNr + "_" + tagNr.toString();
    }

    public void withServiceMetaData(ServiceMetaData serviceMetaData) {
        this.serviceMetaData = serviceMetaData;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class ServiceBuilder {}

    public List<String> retrieveMavenArtifactsAsVcsPath() {
        return retrieveMavenArtifactsToBuild().stream().map(MavenArtifact::getName).collect(Collectors.toList());
    }


    public List<MavenArtifact> retrieveMavenArtifactsToBuild() {
        return artifactsToPatch.stream().filter(m -> m.getVersion().endsWith("SNAPSHOT")).collect(Collectors.toList());
    }
}
