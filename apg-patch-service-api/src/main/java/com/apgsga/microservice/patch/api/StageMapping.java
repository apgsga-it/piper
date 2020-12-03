package com.apgsga.microservice.patch.api;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@JsonDeserialize(builder = StageMapping.StageMappingBuilder.class)
@Value
@Builder
public class StageMapping {

    private String name;
    private String target;

    @JsonPOJOBuilder(withPrefix = "")
    public static class StageMappingBuilder {}
}
