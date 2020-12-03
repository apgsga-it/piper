package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;

import java.util.List;

@JsonDeserialize(builder = StageMappings.StageMappingsBuilder.class)
@Value
@Builder
public class StageMappings {

    private List<StageMapping> stageMappings;

    @JsonPOJOBuilder(withPrefix = "")
    public static class StageMappingsBuilder {}
}
