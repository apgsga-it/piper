package com.apgsga.microservice.patch.core.impl.jenkins;

import com.apgsga.microservice.patch.api.DbObject;
import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.Service;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

@JsonDeserialize(builder = OnCloneBuildParameters.OnCloneBuildParametersBuilder.class)
@Builder
@Value
public class OnCloneBuildParameters {

    String patchNumber;
    String target;
    List<String> dbObjectsAsVcsPath;
    String dbPatchBranch;
    String dbPatchTag;
    List<DbObject> dbObjects;
    List<String> dockerServices;
    List<Service> services;
    List<PackagerInfo> packagers;
    List<String> dbZipNames;
    Map<String, List<MavenArtifact>> artifactsToBuild;

    @JsonPOJOBuilder(withPrefix = "")
    public static class OnCloneBuildParametersBuilder {}
}
