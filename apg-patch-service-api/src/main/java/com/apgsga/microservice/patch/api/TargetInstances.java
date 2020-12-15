package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.List;


@JsonDeserialize(builder = TargetInstances.TargetInstancesBuilder.class)
@Value
@Builder
public class TargetInstances {

    List<TargetInstance> targetInstances;

    @JsonPOJOBuilder(withPrefix = "")
    public static class TargetInstancesBuilder {}
}
