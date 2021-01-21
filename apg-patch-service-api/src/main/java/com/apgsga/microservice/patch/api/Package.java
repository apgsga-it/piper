package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@JsonDeserialize(builder = Package.PackageBuilder.class)
@Value
@Builder
public class Package {
    String pkgServiceName;
    String packagerName;
    List<String> starterCoordinates;


    @JsonPOJOBuilder(withPrefix = "")
    public static class PackageBuilder {}
}
