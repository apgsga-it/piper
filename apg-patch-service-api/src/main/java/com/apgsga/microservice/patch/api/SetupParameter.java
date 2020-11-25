package com.apgsga.microservice.patch.api;

public class SetupParameter {

    private String patchNumber;
    private String successNotification;
    private String errorNotification;

    private SetupParameter() {}

    public static SetupParameter create() {
        return new SetupParameter();
    }

    public SetupParameter patchNumber(String patchNumber) {
        this.patchNumber = patchNumber;
        return this;
    }

    public SetupParameter successNotification(String successNotification) {
        this.successNotification = successNotification;
        return this;
    }

    public SetupParameter errorNotification(String errorNotification) {
        this.errorNotification = errorNotification;
        return this;
    }

    public String getPatchNumber() {
        return patchNumber;
    }

    public String getSuccessNotification() {
        return successNotification;
    }

    public String getErrorNotification() {
        return errorNotification;
    }

    @Override
    public String toString() {
        return "SetupParameter{" +
                "patchNumber='" + patchNumber + '\'' +
                ", successNotification='" + successNotification + '\'' +
                ", errorNotification='" + errorNotification + '\'' +
                '}';
    }
}
