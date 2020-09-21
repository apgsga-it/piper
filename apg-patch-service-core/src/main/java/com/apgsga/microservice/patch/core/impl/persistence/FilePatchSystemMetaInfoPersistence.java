package com.apgsga.microservice.patch.core.impl.persistence;

import com.apgsga.microservice.patch.api.OnDemandTargets;
import com.apgsga.microservice.patch.api.PatchSystemMetaInfoPersistence;
import com.apgsga.microservice.patch.api.StageMappings;
import com.apgsga.microservice.patch.api.TargetInstances;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;

public class FilePatchSystemMetaInfoPersistence extends AbstractFilebasedPersistence implements PatchSystemMetaInfoPersistence {

    private static final String ON_DEMAND_TARGETS_DATA_JSON = "OnDemandTargets.json";

    private static final String STAGE_MAPPINGS_DATA_JSON = "StageMappings.json";

    private static final String TARGET_INSTANCES_DATA_JSON = "TargetInstances.json";

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
    public StageMappings stageMapping() {
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
}
