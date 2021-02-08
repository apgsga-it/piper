package com.apgsga.microservice.patch.core.impl.jenkins;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@JsonDeserialize(builder = OnClonePipelineParameter.OnClonePipelineParameterBuilder.class)
@Builder
@Value
public class OnClonePipelineParameter {

    String src;
    String target;
    List<OnClonePatchParameters> patches;

    @JsonPOJOBuilder(withPrefix = "")
    public static class OnClonePipelineParameterBuilder {}
}
