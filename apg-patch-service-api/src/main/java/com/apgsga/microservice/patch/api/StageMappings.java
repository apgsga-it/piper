package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@JsonDeserialize(builder = StageMappings.StageMappingsBuilder.class)
@Value
@Builder
public class StageMappings {

    List<StageMapping> stageMappings;

    @JsonPOJOBuilder(withPrefix = "")
    public static class StageMappingsBuilder {}
}
