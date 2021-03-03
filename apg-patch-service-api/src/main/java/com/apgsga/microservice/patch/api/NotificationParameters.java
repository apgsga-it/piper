package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@JsonDeserialize(builder = NotificationParameters.NotificationParametersBuilder.class)
@Value
@Builder
public class NotificationParameters {

    String patchNumbers; // separated by a comma if more than one patch
    String installationTarget;
    String notification;

    @JsonPOJOBuilder(withPrefix = "")
    public static class NotificationParametersBuilder {}

}
