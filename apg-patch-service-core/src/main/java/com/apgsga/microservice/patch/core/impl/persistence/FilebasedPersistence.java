package com.apgsga.microservice.patch.core.impl.persistence;

import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;

public interface FilebasedPersistence {

    void init() throws IOException;
    File createFile(String fileName) throws IOException;
    <T> T findFile(File f, Class<T> clazz) throws IOException;

    <T> void writeToFile(T object, String filename);

    Resource getStoragePath();
}
