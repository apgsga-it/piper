package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;


@JsonDeserialize(builder = BuildParameter.BuildParameterBuilder.class)
@Value
@Builder
public class BuildParameter {

    String patchNumber;
    String stageName;
    String successNotification;
    String errorNotification;


    @JsonPOJOBuilder(withPrefix = "")
    public static class BuildParameterBuilder {}
}
