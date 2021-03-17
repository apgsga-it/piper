package com.apgsga.microservice.patch.core.impl.jenkins;

import com.apgsga.microservice.patch.api.Package;
import com.apgsga.microservice.patch.api.*;
import com.apgsga.microservice.patch.exceptions.Asserts;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("unused")
@Profile("live")
@Component("jenkinsPipelinePreprocessor")
public class JenkinsPipelinePreprocessor {

    @SuppressWarnings("unused")
    protected static final Log LOGGER = LogFactory.getLog(JenkinsPipelinePreprocessor.class.getName());


    public static final String ENTWICKLUNG_STAGE = "entwicklung";
    public static final String IT_21_DB_SERVICE_NAME = "it21-db";

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

    public String retrieveTargetHostFor(Package aPackage, String target) {
        Optional<TargetInstance> targetInstanceOptional = backend.targetInstances().getTargetInstances().stream().filter(ti -> ti.getName().equalsIgnoreCase(target)).findFirst();
        Asserts.notNull(targetInstanceOptional,"No targetInstance has been found for %s",target);
        Asserts.isTrue(targetInstanceOptional.isPresent(),"No targetInstance has been found for %s",target);
        TargetInstance targetInstance = targetInstanceOptional.get();
        Optional<ServiceInstallation> serviceInstallationOptional = targetInstance.getServices().stream().filter(s -> s.getServiceName().equalsIgnoreCase(aPackage.getPkgServiceName())).findFirst();
        Asserts.notNull(serviceInstallationOptional,"No Service has been found for %s",aPackage.getPkgServiceName());
        Asserts.isTrue(serviceInstallationOptional.isPresent(),"No Service has been found for %s",aPackage.getPkgServiceName());
        return serviceInstallationOptional.get().getInstallationHost();
    }

    public List<String> retrieveDbZipNames(Set<String> patchNumbers, String target) {
        List<String> dbZipNames = Lists.newArrayList();
        patchNumbers.forEach(patchNumber -> {
            Patch patch = backend.findById(patchNumber);
            if(!patch.getDbPatch().getDbObjects().isEmpty() || !patch.getDockerServices().isEmpty()) {
                dbZipNames.add(patch.getDbPatch().getDbPatchBranch() + "_" + target.toUpperCase() + ".zip");
            }
        });
        return dbZipNames;
    }

    public boolean needInstallDbPatchFor(Set<String> patchNumbers) {
        for(String patchNumber : patchNumbers) {
            Patch patch = backend.findById(patchNumber);
            if(!patch.getDbPatch().getDbObjects().isEmpty() || !patch.getDockerServices().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public List<Package> packagesFor(Service service) {
        return backend.packagesFor(service);
    }

    public List<PackagerInfo> retrievePackagerInfoFor(Set<String> patchNumbers, String target) {
        LOGGER.info("Retrieving packager info for target " + target + " and following patch(es) : " + patchNumbers.toString());
        List<PackagerInfo> packagers = Lists.newArrayList();
        patchNumbers.forEach(number -> {
            retrievePatch(number).getServices().forEach(service -> {
                packagesFor(service).forEach(aPackage -> {
                    if(!packagers.stream().anyMatch(p -> p.name.equals(aPackage.getPackagerName()))) {
                        packagers.add(new PackagerInfo(aPackage.getPackagerName()
                            , retrieveTargetHostFor(aPackage,target)
                            , retrieveVcsBranchFor(service)));
                    }
                });
            });
        });
        return packagers;
    }

    public Map<String,InstallDbObjectsInfos> retrieveDbObjectInfoFor(Set<String> patchNumbers) {
        LOGGER.info("Retrieving dbObject info for following patch(es) : " + patchNumbers.toString());
        Map<String,InstallDbObjectsInfos> installDbObjectsInfos = Maps.newHashMap();
        patchNumbers.forEach(number -> {
            Patch patch = retrievePatch(number);
            InstallDbObjectsInfos dboInfo = new InstallDbObjectsInfos(patch.getDbPatch().getPatchTag(),patch.getDbPatch().getDbPatchBranch());
            patch.getDbPatch().getDbObjects().forEach(dbo -> {
                dboInfo.dbObjectsModuleNames.add(dbo.getModuleName());
            });
            installDbObjectsInfos.put(number,dboInfo);
        });
        return installDbObjectsInfos;
    }

    public String retrieveDbDeployInstallerHost(String target) {
        LOGGER.info("Retrieving db deploy target for target " + target);
        Optional<TargetInstance> ti = backend.targetInstances().getTargetInstances().stream().filter(f -> f.getName().equalsIgnoreCase(target)).findFirst();
        Asserts.notNull(ti,"No targetInstance has been found for %s",target);
        Asserts.isTrue(ti.isPresent(),"No targetInstance has been found for %s",target);
        Optional<ServiceInstallation> si = ti.get().getServices().stream().filter(s -> s.getServiceName().equalsIgnoreCase(IT_21_DB_SERVICE_NAME)).findFirst();
        Asserts.notNull(si,"No Service has been found for %s",IT_21_DB_SERVICE_NAME);
        Asserts.isTrue(si.isPresent(),"No Service has been found for %s",IT_21_DB_SERVICE_NAME);
        return si.get().getInstallationHost();
    }
}
