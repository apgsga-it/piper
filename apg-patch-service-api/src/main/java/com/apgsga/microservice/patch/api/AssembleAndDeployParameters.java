package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.collect.Sets;
import lombok.Builder;
import lombok.Value;

import java.util.LinkedHashSet;
import java.util.Set;

@JsonDeserialize(builder = AssembleAndDeployParameters.AssembleAndDeployParametersBuilder.class)
@Value
@Builder
public class AssembleAndDeployParameters {

    @Builder.Default
    LinkedHashSet<String> patchNumbers = Sets.newLinkedHashSet();
    String target;
    String successNotification;
    String errorNotification;

    @JsonPOJOBuilder(withPrefix = "")
    public static class AssembleAndDeployParametersBuilder {}
}
