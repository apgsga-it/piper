package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.collect.Lists;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@JsonDeserialize(builder = Patch.PatchBuilder.class)
@Value
@Builder
public class Patch {

    private static final String PROD_BRANCH_DEFAULT = "prod";

    String patchNumber;
    String dbPatchBranch;
    @Builder.Default
    String prodBranch = PROD_BRANCH_DEFAULT;
    @Builder.Default
    String patchTag = "";
    @Builder.Default
    String developerBranch = "";
    @Builder.Default
    Integer tagNr = 0;
    @Builder.Default
    List<DbObject> dbObjects = Lists.newArrayList();
    @Builder.Default
    List<String> dockerServices = Lists.newArrayList();
    @Builder.Default
    List<Service> services = Lists.newArrayList();


    // TODO (MULTISERVICE_CM , 9.4) : This is here for backward compatibility and must go away
    public List<MavenArtifact> retrieveAllArtifactsToPatch() {
        return services.stream()
                .flatMap(coll -> coll.getArtifactsToPatch().stream())
                .collect(Collectors.toList());
    }

    @JsonPOJOBuilder(withPrefix = "")
    static class PatchBuilder {
    }
}
