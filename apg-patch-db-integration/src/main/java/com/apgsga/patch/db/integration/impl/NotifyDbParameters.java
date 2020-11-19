package com.apgsga.patch.db.integration.impl;

import com.google.common.collect.Maps;

import java.util.Map;

public class NotifyDbParameters {

    // TODO JHE (19.11.2020): To be confirmed with Ueli that these names are correct
    public static final String PATCH_NOTIFICATION_PROCEDURE_NAME = "cm.cm_patch_notification";
    public static final String PATCH_NUMBER_PARAM = "patchNumber";
    public static final String STAGE_PARAM = "stage";
    public static final String SUCCESS_NOTIFICATION_PARAM = "successNotification";

    private String patchNumber;
    private String stage;
    private String successNotification;

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

    public String getPatchNumber() {
        return patchNumber;
    }

    public String getStage() {
        return stage;
    }

    public String getSuccessNotification() {
        return successNotification;
    }

    public Map<String,String> getAllParameters() {
        Map<String,String> params = Maps.newHashMap();
        params.put(PATCH_NUMBER_PARAM,getPatchNumber());
        if(getStage() != null) {
            params.put(STAGE_PARAM, getStage());
        }
        if(getSuccessNotification() != null) {
            params.put(SUCCESS_NOTIFICATION_PARAM, getSuccessNotification());
        }
        return params;
    }

    @Override
    public String toString() {
        return "NotifyDbParameters{" +
                PATCH_NUMBER_PARAM + "='" + patchNumber + '\'' +
                "," + STAGE_PARAM + "='" + stage + '\'' +
                "," + SUCCESS_NOTIFICATION_PARAM + "='" + successNotification + '\'' +
                '}';
    }
}
