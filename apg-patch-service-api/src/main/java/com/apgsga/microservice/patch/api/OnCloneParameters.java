package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.collect.Sets;
import lombok.Builder;
import lombok.Value;

import java.util.LinkedHashSet;

@JsonDeserialize(builder = OnCloneParameters.OnCloneParametersBuilder.class)
@Value
@Builder
public class OnCloneParameters {

    String src;
    String target;
    @Builder.Default
    LinkedHashSet<String> patchNumbers = Sets.newLinkedHashSet();

    @JsonPOJOBuilder(withPrefix = "")
    public static class OnCloneParametersBuilder {}
}
