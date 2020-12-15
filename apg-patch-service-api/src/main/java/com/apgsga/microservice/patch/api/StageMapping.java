package com.apgsga.microservice.patch.api;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@JsonDeserialize(builder = StageMapping.StageMappingBuilder.class)
@Value
@Builder
public class StageMapping {

    String name;
    String target;

    @JsonPOJOBuilder(withPrefix = "")
    public static class StageMappingBuilder {}
}
