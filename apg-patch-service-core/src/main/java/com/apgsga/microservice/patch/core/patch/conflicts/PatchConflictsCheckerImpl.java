package com.apgsga.microservice.patch.core.patch.conflicts;

import com.apgsga.microservice.patch.api.DbObject;
import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.Patch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;

public class PatchConflictsCheckerImpl implements PatchConflictsChecker {

    protected static final Log LOGGER = LogFactory.getLog(PatchConflictsCheckerImpl.class.getName());

    private List<Patch> patchToBeChecked;

    private PatchConflictsCheckerImpl(){
        patchToBeChecked = Lists.newArrayList();
    }

    public static PatchConflictsChecker create() {
        return new PatchConflictsCheckerImpl();
    }

    @Override
    public void addPatch(Patch p) {
        patchToBeChecked.add(p);
    }

    @Override
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
            Map<String,List<MavenArtifact>> duplicateMavenArtifactsForService = duplicateMavenArtifactsForServices(srcPatch,patchToBeChecked.get(i));

            if(!duplicateDockerServices.isEmpty() || !duplicateDbObjects.isEmpty() || !duplicateMavenArtifactsForService.isEmpty()) {
                result.add(PatchConflict.builder()
                                        .p1(srcPatch)
                                        .p2(patchToBeChecked.get(i))
                                        .dockerServices(duplicateDockerServices)
                                        .dbObjects(duplicateDbObjects)
                                        .serviceWithMavenArtifacts(duplicateMavenArtifactsForService)
                                        .build());
            }
            i++;
        }
        return result;
    }

    private Map<String,List<MavenArtifact>> duplicateMavenArtifactsForServices(Patch srcPatch, Patch comparedPatch) {
        Map<String,List<MavenArtifact>> result = Maps.newHashMap();
        if(srcPatch.getServices() != null && !srcPatch.getServices().isEmpty()) {
            srcPatch.getServices().forEach(srcService -> {
                srcService.getArtifactsToPatch().forEach(srcMa -> {
                    if(comparedPatch.getServices() != null && !comparedPatch.getServices().isEmpty()) {
                        comparedPatch.getServices().forEach(compService -> {
                            if(srcService.getServiceName().equals(compService.getServiceName())) {
                                compService.getArtifactsToPatch().forEach(compMa -> {
                                    // JHE: equals method generated from Lombok annotation
                                    if(srcMa.equals(compMa)) {
                                        if(result.containsKey(srcService.getServiceName())) {
                                            result.get(srcService.getServiceName()).add(srcMa);
                                        }
                                        else {
                                            result.put(srcService.getServiceName(),Lists.newArrayList(srcMa));
                                        }
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
