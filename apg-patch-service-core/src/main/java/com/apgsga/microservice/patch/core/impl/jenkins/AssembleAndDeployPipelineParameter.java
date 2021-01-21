package com.apgsga.microservice.patch.core.impl.jenkins;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Set;

@JsonDeserialize(builder = AssembleAndDeployPipelineParameter.AssembleAndDeployParameterBuilder.class)
@Builder
@Value
public class AssembleAndDeployPipelineParameter {

    Set<String> patchNumbers;
    String target;
    String successNotification;
    String errorNotification;
    List<PackagerInfo> packagers;
    List<String> dbZipNames;

    @JsonPOJOBuilder(withPrefix = "")
    public static class AssembleAndDeployParameterBuilder {}

    public static class PackagerInfo {
        public String name;
        public String targetHost;
        public String vcsBranch;

        public PackagerInfo(String name, String targetHost, String vcsBranch) {
            this.name = name;
            this.targetHost = targetHost;
            this.vcsBranch = vcsBranch;
        }
    }
}
