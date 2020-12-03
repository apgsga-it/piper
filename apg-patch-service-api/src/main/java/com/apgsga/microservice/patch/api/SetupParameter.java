package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@JsonDeserialize(builder = SetupParameter.ServicesMetaDataBuilder.class)
@Value
@Builder
public class SetupParameter {

    private String patchNumber;
    private String successNotification;
    private String errorNotification;

    @JsonPOJOBuilder(withPrefix = "")
    static class ServicesMetaDataBuilder {}
}
