package com.apgsga.microservice.patch.core.patch.conflicts;

import com.apgsga.microservice.patch.api.DbObject;
import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.Patch;
import com.google.common.collect.Lists;

import java.util.List;

public class PatchConflictsChecker {

    private List<Patch> patchToBeChecked;

    private PatchConflictsChecker(){
        patchToBeChecked = Lists.newArrayList();
    }

    public static PatchConflictsChecker create() {
        return new PatchConflictsChecker();
    }

    public void addPatch(Patch p) {
        patchToBeChecked.add(p);
    }

    public List<PatchConflict> checkConflicts() {
        List<PatchConflict> result = Lists.newArrayList();
        int indexForCheck = 0;
        while(indexForCheck < patchToBeChecked.size()-1) {
            result.addAll(checkConflictFor(patchToBeChecked.get(indexForCheck), indexForCheck+1));
            indexForCheck++;
        }
        return result;
    }

    private List<PatchConflict> checkConflictFor(Patch srcPatch, int startComparisonFrom) {
        List<PatchConflict> result = Lists.newArrayList();
        int i = startComparisonFrom;

        while(i < patchToBeChecked.size()) {
            List<String> duplicateDockerServices = ducplicateDockerServicesFor(srcPatch,patchToBeChecked.get(i));
            List<DbObject> duplicateDbObjects = duplicateDbObjectsFor(srcPatch,patchToBeChecked.get(i));
            List<MavenArtifact> duplicateMavenArtifacts = duplicateMavenArtifacts(srcPatch,patchToBeChecked.get(i));

            if(!duplicateDockerServices.isEmpty() || !duplicateDbObjects.isEmpty() || !duplicateMavenArtifacts.isEmpty()) {
                result.add(PatchConflict.create()
                                        .patch1(srcPatch)
                                        .patch2(patchToBeChecked.get(i))
                                        .dockerServices(duplicateDockerServices)
                                        .dbObjects(duplicateDbObjects)
                                        .mavenArtifacts(duplicateMavenArtifacts));
            }
            i++;
        }
        return result;
    }

    private List<MavenArtifact> duplicateMavenArtifacts(Patch srcPatch, Patch comparedPatch) {
        List<MavenArtifact> result = Lists.newArrayList();
        if(srcPatch.getServices() != null && !srcPatch.getServices().isEmpty()) {
            srcPatch.getServices().forEach(srcService -> {
                srcService.getArtifactsToPatch().forEach(srcMa -> {
                    if(comparedPatch.getServices() != null && !comparedPatch.getServices().isEmpty()) {
                        comparedPatch.getServices().forEach(compService -> {
                            if(srcService.getServiceName().equals(compService.getServiceName())) {
                                compService.getArtifactsToPatch().forEach(compMa -> {
                                    // JHE: equals method generated from Lombok annotation
                                    if(srcMa.equals(compMa)) {
                                        result.add(srcMa);
                                    }
                                });
                            }
                        });
                    }
                });
            });
        }
        return result;
    }

    private List<DbObject> duplicateDbObjectsFor(Patch srcPatch, Patch comparedPatch) {
        List<DbObject> result = Lists.newArrayList();
        if(srcPatch.getDbPatch() != null) {
            srcPatch.getDbPatch().getDbObjects().forEach(srcDbo -> {
                // JHE: equals method generated from Lombok annotation
                if (comparedPatch.getDbPatch().getDbObjects().contains(srcDbo)) {
                    result.add(srcDbo);
                }
            });
        }
        return result;
    }

    private List<String> ducplicateDockerServicesFor(Patch srcPatch, Patch comparedPatch) {
        List<String> result = Lists.newArrayList();
        srcPatch.getDockerServices().forEach(d -> {
            if(comparedPatch.getDockerServices().contains(d)) {
                result.add(d);
            }
        });
        return result;
    }


}
