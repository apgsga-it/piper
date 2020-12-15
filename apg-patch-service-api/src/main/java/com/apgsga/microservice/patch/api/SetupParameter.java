package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@JsonDeserialize(builder = SetupParameter.SetupParameterBuilder.class)
@Value
@Builder
public class SetupParameter {

    String patchNumber;
    String successNotification;
    String errorNotification;

    @JsonPOJOBuilder(withPrefix = "")
    public static class SetupParameterBuilder {}
}
