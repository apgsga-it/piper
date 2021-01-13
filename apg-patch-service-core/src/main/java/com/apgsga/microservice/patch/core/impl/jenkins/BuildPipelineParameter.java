package com.apgsga.microservice.patch.core.impl.jenkins;

import com.apgsga.microservice.patch.api.DbObject;
import com.apgsga.microservice.patch.api.Service;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@JsonDeserialize(builder = BuildPipelineParameter.BuildParameterBuilder.class)
@Builder
@Value
public class BuildPipelineParameter {

    String patchNumber;
    String stageName;
    String successNotification;
    String errorNotification;
    String target;
    String patchTag;
    String developerBranch;
    List<String> dbObjectsAsVcsPath;
    String dbPatchBranch;
    String dbPatchTag;
    List<DbObject> dbObjects;
    List<String> dockerServices;
    List<Service> services;

    @JsonPOJOBuilder(withPrefix = "")
    public static class BuildParameterBuilder {}
}
