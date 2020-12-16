package com.apgsga.microservice.patch.core.impl.persistence;

import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;

public class FilebasedPersistenceImpl implements FilebasedPersistence {


    public static FilebasedPersistence create(Resource storagePath, Resource tempStoragePath) {
        return new FilebasedPersistenceImpl(storagePath,tempStoragePath);
    }

    protected final Resource storagePath;

    protected final Resource tempStoragePath;

    public FilebasedPersistenceImpl(Resource storagePath, Resource tempStoragePath)  {
        this.storagePath = storagePath;
        this.tempStoragePath = tempStoragePath;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void init() throws IOException {
        if (!storagePath.exists()) {
            storagePath.getFile().mkdir();
        }

        if (!tempStoragePath.exists()) {
            tempStoragePath.getFile().mkdir();
        }
    }

    public File createFile(String fileName) throws IOException {
        File parentDir = storagePath.getFile();
        return new File(parentDir, fileName);
    }

    public <T> T findFile(File f, Class<T> clazz) throws IOException {
        if (!f.exists()) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(f, clazz);
    }

    public synchronized <T> void writeToFile(T object, String filename) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonRequestString;
        try {
            jsonRequestString = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw ExceptionFactory.create("Persistence Exception : <%s>  on Json Conversation before writing to file %s", e,
                    e.getMessage(), filename);
        }
        AtomicFileWriteManager.create(storagePath, tempStoragePath).write(jsonRequestString, filename);

    }

    @Override
    public Resource getStoragePath() {
        return storagePath;
    }


}
