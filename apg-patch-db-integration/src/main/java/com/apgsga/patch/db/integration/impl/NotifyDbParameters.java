package com.apgsga.patch.db.integration.impl;

import com.google.common.collect.Maps;

import java.util.Map;

public class NotifyDbParameters {

    // TODO JHE (19.11.2020): To be confirmed with Ueli that these names are correct
    public static final String PATCH_NOTIFICATION_PROCEDURE_NAME = "cm.cm_patch_notification";
    public static final String PATCH_NUMBER_PARAM = "patchNumber";
    public static final String STAGE_PARAM = "stage";
    public static final String SUCCESS_OR_ERROR_PARAM = "notification";

    private String patchNumber;
    private String stage;
    private String successNotification;
    private String errorNotification;

    private NotifyDbParameters(){}

    private NotifyDbParameters(String patchNumber){
        this.patchNumber = patchNumber;
    };

    public static NotifyDbParameters create(String patchNumber) {
        NotifyDbParameters param = new NotifyDbParameters(patchNumber);
        return param;
    }

    public NotifyDbParameters stage(String stage) {
        this.stage = stage;
        return this;
    }

    public NotifyDbParameters successNotification(String successNotification) {
        this.successNotification = successNotification;
        return this;
    }

    public NotifyDbParameters errorNotification(String errorNotification) {
        this.errorNotification = errorNotification;
        return this;
    }

    public String getPatchNumber() {
        return patchNumber;
    }

    public String getStage() {
        return stage;
    }

    public String getSuccessNotification() {
        return successNotification;
    }

    public String getErrorNotification() {
        return errorNotification;
    }

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

    @Override
    public String toString() {
        return "NotifyDbParameters{" +
                "patchNumber='" + patchNumber + '\'' +
                ", stage='" + stage + '\'' +
                ", successNotification='" + successNotification + '\'' +
                ", errorNotification='" + errorNotification + '\'' +
                '}';
    }
}
