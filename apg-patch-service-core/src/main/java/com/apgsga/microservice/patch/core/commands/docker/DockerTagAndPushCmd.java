package com.apgsga.microservice.patch.core.commands.docker;

import com.apgsga.microservice.patch.core.commands.CommandBaseImpl;

import java.util.List;

public class DockerTagAndPushCmd extends CommandBaseImpl {

    protected final String pathToDockerScript;

    protected final List<String> dockerServiceNames;

    protected final String patchNumber;

    public DockerTagAndPushCmd(String pathToDockerScript, List<String> dockerServiceNames, String patchNumber) {
        this.pathToDockerScript = pathToDockerScript;
        this.dockerServiceNames = dockerServiceNames;
        this.patchNumber = patchNumber;
    }

    @Override
    protected String[] getParameterAsArray() {
        return new String[] {pathToDockerScript,String.join(",",dockerServiceNames),patchNumber};
    }

    @Override
    protected String getParameterSpaceSeperated() {
        LOGGER.warn(DockerTagAndPushCmd.class.getName() + " not implemented for Windows !!");
        return null;
    }
}
