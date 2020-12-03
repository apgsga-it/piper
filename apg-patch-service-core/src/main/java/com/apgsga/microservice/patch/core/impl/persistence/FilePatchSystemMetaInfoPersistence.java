package com.apgsga.microservice.patch.core.impl.persistence;

import com.apgsga.microservice.patch.api.*;
import com.apgsga.microservice.patch.api.Package;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FilePatchSystemMetaInfoPersistence extends AbstractFilebasedPersistence implements PatchSystemMetaInfoPersistence {

    private static final String ON_DEMAND_TARGETS_DATA_JSON = "OnDemandTargets.json";

    private static final String STAGE_MAPPINGS_DATA_JSON = "StageMappings.json";

    private static final String TARGET_INSTANCES_DATA_JSON = "TargetInstances.json";

    private static final String SERVICES_METADATA_DATA_JSON = "ServicesMetaData.json";

    public FilePatchSystemMetaInfoPersistence(Resource storagePath, Resource workDir) throws IOException {
        super();
        this.storagePath = storagePath;
        this.tempStoragePath = workDir;
        init();
    }

    @Override
    public OnDemandTargets onDemandTargets() {
        try {
            File onDemandTargetFile = createFile(ON_DEMAND_TARGETS_DATA_JSON);
            if (!onDemandTargetFile.exists()) {
                return null;
            }
            ObjectMapper mapper = new ObjectMapper();
            OnDemandTargets result = mapper.readValue(onDemandTargetFile, OnDemandTargets.class);
            return result;
        } catch (IOException e) {
            throw ExceptionFactory.createPatchServiceRuntimeException(
                    "FilebasedPatchPersistence.onDemandTargets.exception", new Object[] { e.getMessage() }, e);
        }
    }

    @Override
    public StageMappings stageMappings() {
        try {
            File stageMappingFile = createFile(STAGE_MAPPINGS_DATA_JSON);
            if (!stageMappingFile.exists()) {
                return null;
            }
            ObjectMapper mapper = new ObjectMapper();
            StageMappings result = mapper.readValue(stageMappingFile, StageMappings.class);
            return result;
        } catch (IOException e) {
            throw ExceptionFactory.createPatchServiceRuntimeException(
                    "FilebasedPatchPersistence.stageMapping.exception", new Object[] { e.getMessage() }, e);
        }
    }

    @Override
    public TargetInstances targetInstances() {
        try {
            File targetInstanceFile = createFile(TARGET_INSTANCES_DATA_JSON);
            if (!targetInstanceFile.exists()) {
                return null;
            }
            ObjectMapper mapper = new ObjectMapper();
            TargetInstances result = mapper.readValue(targetInstanceFile, TargetInstances.class);
            return result;
        } catch (IOException e) {
            throw ExceptionFactory.createPatchServiceRuntimeException(
                    "FilebasedPatchPersistence.targetInstance.exception", new Object[] { e.getMessage() }, e);
        }
    }

    @Override
    public ServicesMetaData servicesMetaData() {
        try {
            File servicesMetadata = createFile(SERVICES_METADATA_DATA_JSON);
            if (!servicesMetadata.exists()) {
                return null;
            }
            ObjectMapper mapper = new ObjectMapper();
            ServicesMetaData result = mapper.readValue(servicesMetadata, ServicesMetaData.class);
            return result;
        } catch (IOException e) {
            throw ExceptionFactory.createPatchServiceRuntimeException(
                    "FilebasedPatchPersistence.servicemetadata.exception", new Object[] { e.getMessage() }, e);
        }
    }

    @Override
    public List<Package> packagesFor(Service service) {
        for(ServiceMetaData smd : servicesMetaData().getServicesMetaData()) {
            if(smd.getServiceName().equals(service.getServiceName())) {
                return smd.getPackages();
            }
        }
        return null;
    }

    @Override
    public String targetFor(String stageName) {
        StageMappings sms = stageMappings();
        for(StageMapping sm : sms.getStageMappings()) {
            if(sm.getName().equalsIgnoreCase(stageName)) {
                return sm.getTarget();
            }
        }
        return null;
    }
}
