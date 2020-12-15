package com.apgsga.microservice.patch.api;

import com.google.common.collect.Sets;

import java.util.Set;

// TODO (jhe, 15.12 ) :  move to Lombok and only API Object's
public class AssembleAndDeployParameters {

    private Set<String> patches = Sets.newHashSet();
    // TODO (jhe, 15.12) I don't think below should be part of the api
    private Set<String> gradlePackagerProjectAsVscPath = Sets.newHashSet();
    private String target;
    private String successNotification;
    private String errorNotification;

    private AssembleAndDeployParameters(){}

    public static AssembleAndDeployParameters create() {
        return new AssembleAndDeployParameters();
    }

    public AssembleAndDeployParameters target(String target) {
        this.target = target;
        return this;
    }

    public AssembleAndDeployParameters successNotification(String successNotification) {
        this.successNotification = successNotification;
        return this;
    }

    public AssembleAndDeployParameters errorNotification(String errorNotification) {
        this.errorNotification = errorNotification;
        return this;
    }

    public AssembleAndDeployParameters addPatchNumber(String patchNumber) {
        this.patches.add(patchNumber);
        return this;
    }

    public AssembleAndDeployParameters addGradlePackageProjectAsVcsPath(String pathToPackage) {
        this.gradlePackagerProjectAsVscPath.add(pathToPackage);
        return this;
    }

    public Set<String> getPatches() {
        return patches;
    }

    public String getTarget() {
        return target;
    }

    public String getSuccessNotification() {
        return successNotification;
    }

    public String getErrorNotification() {
        return errorNotification;
    }

    public Set<String> getGradlePackagerProjectAsVscPath() {
        return gradlePackagerProjectAsVscPath;
    }

    @Override
    public String toString() {
        return "AssembleAndDeployParameters{" +
                "patches=" + patches +
                ", gradlePackagerProjectAsVscPath=" + gradlePackagerProjectAsVscPath +
                ", target='" + target + '\'' +
                ", successNotification='" + successNotification + '\'' +
                ", errorNotification='" + errorNotification + '\'' +
                '}';
    }
}
