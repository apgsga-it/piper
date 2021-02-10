package com.apgsga.microservice.patch.core.impl.jenkins;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Set;

@JsonDeserialize(builder = OnCloneAssembleAndDeployParameter.OnCloneAssembleAndDeployParameterBuilder.class)
@Builder
@Value
public class OnCloneAssembleAndDeployParameter {

    Set<String> patchNumbers;
    String target;
    List<PackagerInfo> packagers;
    List<String> dbZipNames;

    @JsonPOJOBuilder(withPrefix = "")
    public static class OnCloneAssembleAndDeployParameterBuilder {}
}
