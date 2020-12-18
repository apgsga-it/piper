package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.collect.Sets;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

@JsonDeserialize(builder = InstallParameters.InstallParametersBuilder.class)
@Value
@Builder
public class InstallParameters {

    @Builder.Default
    Set<String> patchNumbers = Sets.newHashSet();
    String target;
    String successNotification;
    String errorNotification;

    @JsonPOJOBuilder(withPrefix = "")
    public static class InstallParametersBuilder {}
}
