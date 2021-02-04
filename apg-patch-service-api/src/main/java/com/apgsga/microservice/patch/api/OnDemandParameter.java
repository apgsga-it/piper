package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@JsonDeserialize(builder = OnDemandParameter.OnDemandParameterBuilder.class)
@Value
@Builder
public class OnDemandParameter {

    String patchNumber;
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
    public static class OnDemandParameterBuilder {}
}
