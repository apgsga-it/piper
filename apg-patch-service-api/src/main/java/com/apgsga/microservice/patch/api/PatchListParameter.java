package com.apgsga.microservice.patch.api;

// JHE (04.05.2021) : When apscli gets called from outside, we might need additional information about a patch (any information store in the Workflow db)
//                    The external callers will call the different apscli method by passing a JSON String which can be deserialize in this class

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@JsonDeserialize(builder = PatchListParameter.PatchListParameterBuilder.class)
public class PatchListParameter {

    String patchNumber;
    List<String> emails;

    @JsonPOJOBuilder(withPrefix = "")
    public static class PatchListParameterBuilder {}

}
