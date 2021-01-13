package com.apgsga.microservice.patch.core.impl.jenkins;

import com.apgsga.microservice.patch.api.Package;
import com.apgsga.microservice.patch.api.*;
import com.apgsga.microservice.patch.exceptions.Asserts;
import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
@Profile("live")
@Component("jenkinsPipelinePreprocessor")
public class JenkinsPipelinePreprocessor {

    @SuppressWarnings("unused")
    protected static final Log LOGGER = LogFactory.getLog(JenkinsPipelinePreprocessor.class.getName());


    public static final String ENTWICKLUNG_STAGE = "entwicklung";

    @SuppressWarnings("unused")
    @Autowired
    @Qualifier("patchPersistence")
    private PatchPersistence backend;

    public String retrieveStagesTargetAsCSV() {
        StringBuilder stagesAsCSV = new StringBuilder();
        for (StageMapping sm : backend.stageMappings().getStageMappings()) {
            if (!sm.getName().equalsIgnoreCase(ENTWICKLUNG_STAGE)) {
                stagesAsCSV.append(sm.getName()).append(",");
            }
        }
        return stagesAsCSV.substring(0, stagesAsCSV.length() - 1);
    }

    public String retrieveTargetForStageName(String stageName) {
        return backend.targetFor(stageName);
    }

    public Patch retrievePatch(String patchNumber) {
        return backend.findById(patchNumber);
    }

    public String retrieveVcsBranchFor(Service service) {
        return  backend.getServiceMetaDataByName(service.getServiceName()).getMicroServiceBranch();
    }

    public String retrieveBaseVersionFor(Service service) {
        String baseVersionNumber = backend.getServiceMetaDataByName(service.getServiceName()).getBaseVersionNumber();
        String mnemoPart = backend.getServiceMetaDataByName(service.getServiceName()).getRevisionMnemoPart();
        return baseVersionNumber + "-" + mnemoPart;
    }

    public String retrieveTargetHostFor(Service service, String target) {
        TargetInstance targetInstance = backend.targetInstances().getTargetInstances().stream().filter(ti -> ti.getName().toUpperCase().equals(target.toUpperCase())).findFirst().get();
        Asserts.notNullOrEmpty("No targetInstance has been found for %s",target);
        return targetInstance.getServices().stream().filter(s -> s.getServiceName().toUpperCase().equals(service.getServiceName().toUpperCase())).findFirst().get().getInstallationHost();
    }

    public List<String> retrieveDbZipNames(Set<String> patchNumbers, String target) {
        List<String> dbZipNames = Lists.newArrayList();
        patchNumbers.forEach(patchNumber -> {
            Patch patch = backend.findById(patchNumber);
            if(!patch.getDbPatch().getDbObjects().isEmpty()) {
                dbZipNames.add(patch.getDbPatch().getDbPatchBranch() + "_" + target.toUpperCase() + ".zip");
            }
        });
        return dbZipNames;
    }

    public List<Package> packagesFor(Service service) {
        return backend.packagesFor(service);
    }
}
