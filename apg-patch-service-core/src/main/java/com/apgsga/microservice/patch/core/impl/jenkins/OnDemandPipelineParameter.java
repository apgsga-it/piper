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

@JsonDeserialize(builder = OnDemandPipelineParameter.OnDemandPipelineParameterBuilder.class)
@Builder
@Value
public class OnDemandPipelineParameter {

    String patchNumber;
    String target;
    String developerBranch;
    List<String> dbObjectsAsVcsPath;
    String dbPatchBranch;
    String dbPatchTag;
    List<DbObject> dbObjects;
    List<String> dockerServices;
    List<Service> services;
    List<PackagerInfo> packagers;
    List<String> dbZipNames;
    String dbZipDeployTarget;
    Boolean installDbPatch;
    Boolean installDockerServices;
    String pathToDockerInstallScript;
    String dbZipInstallFrom;
    Map<String, List<MavenArtifact>> artifactsToBuild;

    @JsonPOJOBuilder(withPrefix = "")
    public static class OnDemandPipelineParameterBuilder {}
}
