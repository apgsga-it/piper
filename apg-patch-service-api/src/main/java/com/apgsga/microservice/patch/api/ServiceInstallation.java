package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@JsonDeserialize(builder = ServiceInstallation.ServiceInstallationBuilder.class)
@Value
@Builder(toBuilder = true)
public class ServiceInstallation {

    String installationHost;
    String serviceName;
    String serviceType;


    @JsonPOJOBuilder(withPrefix = "")
    public static class ServiceInstallationBuilder {}
}
