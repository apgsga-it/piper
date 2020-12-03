package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;


@JsonDeserialize(builder = BuildParameter.BuildParameterBuilder.class)
@Value
@Builder
public class BuildParameter {

    private String patchNumber;
    private String stageName;
    private String target;
    private String successNotification;
    private String errorNotification;


    @JsonPOJOBuilder(withPrefix = "")
    static class BuildParameterBuilder {}
}
