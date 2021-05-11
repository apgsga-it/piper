package com.apgsga.microservice.patch.core.patch.conflicts;

import com.apgsga.microservice.patch.api.DbObject;
import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.Patch;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@Builder
@JsonDeserialize(builder = PatchConflict.PatchConflictBuilder.class)
public class PatchConflict {

    Patch p1;
    Patch p2;
    Map<String,List<MavenArtifact>> serviceWithMavenArtifacts;
    List<DbObject> dbObjects;
    List<String> dockerServices;

    @JsonPOJOBuilder(withPrefix = "")
    public static class PatchConflictBuilder{}


}
