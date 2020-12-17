package com.apgsga.microservice.patch.core.impl.jenkins;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

@JsonDeserialize(builder = AssembleAndDeployPipelineParameter.AssembleAndDeployParameterBuilder.class)
@Builder
@Value
public class AssembleAndDeployPipelineParameter {

    Set<String> patchNumbers;
    String target;
    String successNotification;
    String errorNotification;
    Set<String> gradlePackagerProjectAsVscPath;

    @JsonPOJOBuilder(withPrefix = "")
    public static class AssembleAndDeployParameterBuilder {}

}
