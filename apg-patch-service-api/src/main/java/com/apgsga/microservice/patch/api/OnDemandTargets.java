package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;
import java.util.List;


@JsonDeserialize(builder = OnDemandTargets.OnDemandTargetsBuilder.class)
@Value
@Builder
public class OnDemandTargets {

    private List<String> onDemandTargets;

    @JsonPOJOBuilder(withPrefix = "")
    static class OnDemandTargetsBuilder {}
}
