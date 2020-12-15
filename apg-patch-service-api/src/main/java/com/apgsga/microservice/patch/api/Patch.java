package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.collect.Lists;
import lombok.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@JsonDeserialize(builder = Patch.PatchBuilder.class)
@Value
@Builder(toBuilder = true)
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


    // TODO (MULTI_SERVICE_CM , 9.4) : This is here for backward compatibility and must go away
    public List<MavenArtifact> retrieveAllArtifactsToPatch() {
        return services.stream()
                .flatMap(coll -> coll.getArtifactsToPatch().stream())
                .collect(Collectors.toList());
    }

    public List<String> retrieveDbObjectsAsVcsPath() {
        return dbObjects.stream().map(DbObject::asFullPath).collect(Collectors.toList());
    }

    public Service getService(String serviceName) {
        Optional<Service> result = services
                .stream()
                .filter(s -> s.getServiceName().equals(serviceName)).findAny();
        if (result.isPresent()) {
            return result.get();
        }
        return null;
    }


    @JsonPOJOBuilder(withPrefix = "")
    public static class PatchBuilder {
    }
}
