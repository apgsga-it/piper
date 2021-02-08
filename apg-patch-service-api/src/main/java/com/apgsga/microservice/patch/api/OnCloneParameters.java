package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

@JsonDeserialize(builder = OnCloneParameters.OnCloneParametersBuilder.class)
@Value
@Builder
public class OnCloneParameters {

    String src;
    String target;
    Set<String> patchNumbers;

    @JsonPOJOBuilder(withPrefix = "")
    public static class OnCloneParametersBuilder {}
}
