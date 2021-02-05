package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@JsonDeserialize(builder = OnDemandParameter.OnDemandParameterBuilder.class)
@Value
@Builder
public class OnDemandParameter {

    String patchNumber;
    String target;

    @JsonPOJOBuilder(withPrefix = "")
    public static class OnDemandParameterBuilder {}
}
