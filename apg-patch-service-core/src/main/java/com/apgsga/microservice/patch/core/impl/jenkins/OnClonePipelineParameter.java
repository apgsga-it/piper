package com.apgsga.microservice.patch.core.impl.jenkins;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@JsonDeserialize(builder = OnClonePipelineParameter.OnClonePipelineParameterBuilder.class)
@Builder
@Value
public class OnClonePipelineParameter {

    String src;
    String target;
    List<OnCloneBuildParameters> buildParameters;
    OnCloneAssembleAndDeployParameter adParameters;

    @JsonPOJOBuilder(withPrefix = "")
    public static class OnClonePipelineParameterBuilder {}
}
