package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;

import java.util.List;

@JsonDeserialize(builder = Package.PackageBuilder.class)
@Value
@Builder
public class Package {
    private String packagerName;
    private List<String> starterCoordinates;


    @JsonPOJOBuilder(withPrefix = "")
    static class PackageBuilder {}
}
