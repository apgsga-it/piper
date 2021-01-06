package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.List;


@JsonDeserialize(builder = OnDemandTargets.OnDemandTargetsBuilder.class)
@Value
@Builder
public class OnDemandTargets {

    List<String> onDemandTargets;

    @JsonPOJOBuilder(withPrefix = "")
    public static class OnDemandTargetsBuilder {}
}
