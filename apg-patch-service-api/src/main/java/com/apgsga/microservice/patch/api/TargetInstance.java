package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@JsonDeserialize(builder = TargetInstance.TargetInstanceBuilder.class)
@Value
@Builder
public class TargetInstance {

    private String name;
    private List<ServiceMetaData> services;

    @JsonPOJOBuilder(withPrefix = "")
    public static class TargetInstanceBuilder {}

}
