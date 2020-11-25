package com.apgsga.microservice.patch.api;

public class BuildParameter {

    private String patchNumber;
    private String stageName;
    private String target;
    private String successNotification;
    private String errorNotification;

    private BuildParameter(){}

    public static BuildParameter create() {
        return new BuildParameter();
    }

    public BuildParameter patchNumber(String patchNumber) {
        this.patchNumber = patchNumber;
        return this;
    }

    public BuildParameter stageName(String stageName) {
        this.stageName = stageName;
        return this;
    }

    public BuildParameter successNotification(String successNotification) {
        this.successNotification = successNotification;
        return this;
    }

    public BuildParameter errorNotification(String errorNotification) {
        this.errorNotification = errorNotification;
        return this;
    }

    public BuildParameter target(String target) {
        this.target = target;
        return this;
    }

    public String getPatchNumber() {
        return patchNumber;
    }

    public String getStageName() {
        return stageName;
    }

    public String getTarget() { return target; }

    public String getSuccessNotification() {
        return successNotification;
    }

    public String getErrorNotification() {
        return errorNotification;
    }

    @Override
    public String toString() {
        return "BuildParameter{" +
                "patchNumber='" + patchNumber + '\'' +
                ", stageName='" + stageName + '\'' +
                ", target='" + target + '\'' +
                ", successNotification='" + successNotification + '\'' +
                ", errorNotification='" + errorNotification + '\'' +
                '}';
    }
}
