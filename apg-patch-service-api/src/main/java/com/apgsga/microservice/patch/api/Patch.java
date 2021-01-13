package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@JsonDeserialize(builder = Patch.PatchBuilder.class)
@Value
@Builder(toBuilder = true)
@EqualsAndHashCode(exclude = {"patchTag","developerBranch","tagNr","dbPatch","dockerServices","services"})
public class Patch {

    String patchNumber;
    @Builder.Default
    String patchTag = "";
    @Builder.Default
    String developerBranch = "";
    @Builder.Default
    Integer tagNr = 0;
    DBPatch dbPatch;
    @Builder.Default
    List<String> dockerServices = Lists.newArrayList();
    @Builder.Default
    List<Service> services = Lists.newArrayList();


    public List<MavenArtifact> retrieveAllArtifactsToPatch() {
        return services.stream()
                .flatMap(coll -> coll.getArtifactsToPatch().stream())
                .collect(Collectors.toList());
    }

    public List<String> retrieveDbObjectsAsVcsPath() {
        return dbPatch.dbObjects.stream().map(DbObject::asFullPath).collect(Collectors.toList());
    }

    public Service getService(String serviceName) {
        Optional<Service> result = services
                .stream()
                .filter(s -> s.getServiceName().equals(serviceName)).findAny();
        return result.orElse(null);
    }


    @JsonPOJOBuilder(withPrefix = "")
    public static class PatchBuilder {
    }
}
