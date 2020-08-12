package com.apgsga.system.mapping.impl;

import com.apgsga.system.mapping.api.TargetSystemMapping;
import org.springframework.util.Assert;

import java.io.File;
import java.nio.file.Files;

// JHE: Consider writing the implementation in Groovy, might be easier to parse the JSON file

public class TargetSystemMappingImpl implements TargetSystemMapping {

    private File tsmFile;

    private TargetSystemMappingImpl(String tsmFilePath) {
        tsmFile = new File(tsmFilePath);
        Assert.isTrue(Files.exists(tsmFile.toPath()),tsmFilePath + " does not exist!!");
    }

    public static TargetSystemMapping create(String tsmFilePath) {
        return new TargetSystemMappingImpl(tsmFilePath);
    }

    @Override
    public String findStatus(String toStatus) {
        // TODO JHE
        return null;
    }

    @Override
    public String serviceTypeFor(String serviceName, String target) {
        // TODO JHE
        return null;
    }

    @Override
    public String installTargetFor(String serviceName, String target) {
        // TODO JHE
        return null;
    }

    @Override
    public boolean isLightInstance(String target) {
        // TODO JHE
        return false;
    }
}
