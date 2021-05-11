package com.apgsga.microservice.patch.core.impl.jenkins;

import com.apgsga.microservice.patch.core.patch.conflicts.PatchConflict;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

@JsonDeserialize(builder = NotificationForPatchConflictPipelineParameters.NotificationForPatchConflictPipelineParametersBuilder.class)
@Builder
@Value
public class NotificationForPatchConflictPipelineParameters {

    private Set<String> emailAdress;
    private PatchConflict patchConflict;

    @JsonPOJOBuilder(withPrefix = "")
    public static class NotificationForPatchConflictPipelineParametersBuilder {}
}
