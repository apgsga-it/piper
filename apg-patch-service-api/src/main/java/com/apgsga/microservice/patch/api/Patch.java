package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@JsonDeserialize(builder = Patch.PatchBuilder.class)
@Builder(toBuilder = true)
@EqualsAndHashCode(exclude = {"developerBranch","tagNr","dbPatch","dockerServices","services"})
public class Patch {

    @Getter
    String patchNumber;
    @Builder.Default
    @Getter
    String developerBranch = "";
    @Builder.Default
    @Getter
    Integer tagNr = 0;
    @Getter
    DBPatch dbPatch;
    @Builder.Default
    @Getter
    List<String> dockerServices = Lists.newArrayList();
    @Builder.Default
    @Getter
    List<Service> services = Lists.newArrayList();

    public void nextTagNr() {
        this.tagNr = this.tagNr + 1;
    }

    public List<MavenArtifact> retrieveAllArtifactsToPatch() {
        return services.stream()
                .flatMap(coll -> coll.getArtifactsToPatch().stream())
                .collect(Collectors.toList());
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
