package com.apgsga.microservice.patch.core.impl.persistence;

import com.apgsga.microservice.patch.api.*;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;

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
    public StageMapping stageMappingFor(String toStatus) {
        StageMappings stageMappings = stageMappings();
        for(StageMapping stageMapping : stageMappings.getStageMappings()) {
            for(Stage stage : stageMapping.getStages()) {
                if((stageMapping.getName() + stage.getToState()).equals(toStatus)) {
                    return stageMapping;
                }
            }
        }
        return null;
    }

    @Override
    public Integer findStatus(String toStatus) {
        StageMappings stageMappings = stageMappings();
        for(StageMapping stageMapping : stageMappings.getStageMappings()) {
            for(Stage stage : stageMapping.getStages()) {
                if((stageMapping.getName() + stage.getToState()).equals(toStatus)) {
                    return Integer.valueOf(stage.getCode());
                }
            }
        }
        return null;

    }

    @Override
    public String packagerNameFor(Service service) {
        for(ServiceMetaData smd : servicesMetaData().getServicesMetaData()) {
            if(smd.getServiceName().equals(service.getServiceName())) {
                return smd.getPackagerName();
            }
        }
        return null;
    }
}
