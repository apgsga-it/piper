package com.apgsga.microservice.patch.core.impl.jenkins;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Set;

@JsonDeserialize(builder = AssembleAndDeployPipelineParameter.AssembleAndDeployParameterBuilder.class)
@Builder
@Value
public class AssembleAndDeployPipelineParameter {

    Set<String> patchNumbers;
    String target;
    String successNotification;
    String errorNotification;
    List<PackagerInfo> packagers;
    List<String> dbZipNames;
    String dbZipDeployTarget;

    @JsonPOJOBuilder(withPrefix = "")
    public static class AssembleAndDeployParameterBuilder {}

}
