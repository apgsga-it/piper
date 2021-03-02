package com.apgsga.microservice.patch.core.impl.jenkins;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@JsonDeserialize(builder = InstallPipelineParameter.InstallPipelineParameterBuilder.class)
@Builder
@Value
public class InstallPipelineParameter {

    LinkedHashSet<String> patchNumbers;
    String target;
    String successNotification;
    String errorNotification;
    List<PackagerInfo> packagers;
    Boolean installDbPatch;

    @JsonPOJOBuilder(withPrefix = "")
    public static class InstallPipelineParameterBuilder {}
}
