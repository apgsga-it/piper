package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.collect.Maps;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

@JsonDeserialize(builder = NotificationParameters.NotificationParametersBuilder.class)
@Value
@Builder
public class NotificationParameters {

    // TODO JHE (19.11.2020): To be confirmed with Ueli that these names are correct
    public static final String PATCH_NOTIFICATION_PROCEDURE_NAME = "cm.cm_patch_notification";
    public static final String PATCH_NUMBER_PARAM = "patchNumber";
    public static final String STAGE_PARAM = "stage";
    public static final String SUCCESS_OR_ERROR_PARAM = "notification";

    String patchNumber;
    String stage;
    String successNotification;
    String errorNotification;


    public Map<String,String> getAllParameters() {
        Map<String,String> params = Maps.newHashMap();
        params.put(PATCH_NUMBER_PARAM,getPatchNumber());
        if(getStage() != null) {
            params.put(STAGE_PARAM, getStage());
        }
        if(getSuccessNotification() != null && !getSuccessNotification().isEmpty()) {
            params.put(SUCCESS_OR_ERROR_PARAM, getSuccessNotification());
        } else {
            params.put(SUCCESS_OR_ERROR_PARAM,getErrorNotification());
        }
        return params;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class NotificationParametersBuilder {}

}
