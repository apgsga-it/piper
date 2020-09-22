package com.apgsga.microservice.patch.core.impl.persistence;

import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;

public abstract class AbstractFilebasedPersistence {

    protected Resource storagePath;

    protected Resource tempStoragePath;

    protected void init() throws IOException {
        if (!storagePath.exists()) {
            storagePath.getFile().mkdir();
        }

        if (!tempStoragePath.exists()) {
            tempStoragePath.getFile().mkdir();
        }
    }

    protected File createFile(String fileName) throws IOException {
        File parentDir = storagePath.getFile();
        File revisions = new File(parentDir, fileName);
        return revisions;
    }

    protected <T> T findFile(File f, Class<T> clazz) throws IOException {
        if (!f.exists()) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        T patchData = mapper.readValue(f, clazz);
        return patchData;
    }

    protected synchronized <T> void writeToFile(T object, String filename, AbstractFilebasedPersistence filebasedPersistence) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonRequestString;
        try {
            jsonRequestString = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw ExceptionFactory.createPatchServiceRuntimeException("FilebasedPatchPersistence.writeToFile.exception",
                    new Object[] { e.getMessage(), filename }, e);
        }
        AtomicFileWriteManager.create(filebasedPersistence).write(jsonRequestString, filename);

    }

    protected Resource getStoragePath() {
        return storagePath;
    }

    protected Resource getTempStoragePath() {
        return tempStoragePath;
    }


}
