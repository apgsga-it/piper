package com.apgsga.microservice.patch.core.impl.jenkins;

import com.apgsga.microservice.patch.api.DbObject;
import com.apgsga.microservice.patch.api.Service;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;

import java.util.List;

@JsonDeserialize(builder = PipelineBuildParameter.BuildParameterBuilder.class)
@Builder
@Value
public class PipelineBuildParameter {

    String patchNumber;
    String stageName;
    String successNotification;
    String errorNotification;
    String target;
    String patchTag;
    List<String> dbObjectAsVcsPath;
    String dbPatchBranch;
    List<DbObject> dbObjects;
    List<String> dockerServices;
    List<Service> services;

    @JsonPOJOBuilder(withPrefix = "")
    public static class BuildParameterBuilder {}
}
