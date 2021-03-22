package com.apgsga.microservice.patch.core.impl.jenkins;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@JsonDeserialize(builder = InstallPipelineParameter.InstallPipelineParameterBuilder.class)
@Builder
@Value
public class InstallPipelineParameter {

    LinkedHashSet<String> patchNumbers;
    String target;
    String successNotification;
    String errorNotification;
    List<PackagerInfo> packagers;
    Boolean installDbPatch;
    Boolean installDockerServices;
    String dbZipInstallFrom;
    Boolean isProductionInstallation;
    Map<String,InstallDbObjectsInfos> installDbObjectsInfos; // Key is a patchNumber
    String pathToDockerInstallScript;

    @JsonPOJOBuilder(withPrefix = "")
    public static class InstallPipelineParameterBuilder {}
}
