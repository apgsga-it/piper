package com.apgsga.microservice.patch.core.impl;

import com.apgsga.microservice.patch.api.*;
import com.apgsga.microservice.patch.core.commands.CommandRunner;
import com.apgsga.microservice.patch.core.commands.patch.vcs.PatchSshCommand;
import com.apgsga.microservice.patch.api.NotificationParameters;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PatchSetupTask implements Runnable {

    protected static final Log LOGGER = LogFactory.getLog(PatchSetupTask.class.getName());

    public static Runnable create(CommandRunner jschSession, Patch patch, PatchPersistence repo, SetupParameter setupParams) {
        return new PatchSetupTask(jschSession, patch, repo, setupParams);
    }

    private final CommandRunner jschSession;
    private final Patch patch;
    private final PatchPersistence repo;
    private final SetupParameter setupParams;

    private PatchSetupTask(CommandRunner jschSession, Patch patch, PatchPersistence repo, SetupParameter setupParams) {
        super();
        this.jschSession = jschSession;
        this.patch = patch;
        this.repo = repo;
        this.setupParams = setupParams;
    }

    @Override
    public void run() {
        try {
            Integer lastTagNr = patch.getTagNr();
            Integer nextTagNr = lastTagNr++;
            String patchBranch = patch.getDbPatchBranch();
            String nextPatchTag = patchBranch + "_" + nextTagNr.toString();
            LOGGER.info("Patch Setup Task started for patch " + patch.getPatchNumber());
            jschSession.preProcess();
            if (!patch.retrieveDbObjectsAsVcsPath().isEmpty()) {
                LOGGER.info("Creating Tag for DB Objects for patch " + patch.getPatchNumber());
                jschSession.run(PatchSshCommand.createTagPatchModulesCmd(nextPatchTag, patch.getDbPatchBranch(),
                        patch.retrieveDbObjectsAsVcsPath()));
            }
            for (Service service : patch.getServices()) {
                if (!service.retrieveMavenArtifactsAsVcsPath().isEmpty()) {
                    LOGGER.info("Creating Tag for Java Artifact for patch " + patch.getPatchNumber() + " and service " + service.getServiceName());
                    ServiceMetaData serviceMetaData = repo.getServiceMetaDataByName(service.getServiceName());
                    jschSession.run(PatchSshCommand.createTagPatchModulesCmd(nextPatchTag, serviceMetaData.getMicroServiceBranch(),
                            service.retrieveMavenArtifactsAsVcsPath()));
                }
            }
            jschSession.postProcess();
            repo.savePatch(patch.toBuilder().patchTag(nextPatchTag).tagNr(nextTagNr).build());
            LOGGER.info("Patch Setup Task started for patch " + patch.getPatchNumber() + " DONE !! -> notifying db accordingly!");
            repo.notify(NotificationParameters.builder().patchNumber(patch.getPatchNumber()).successNotification(setupParams.getSuccessNotification()).build());
        } catch (Exception e) {
            LOGGER.error("Patch Setup Task for patch " + patch.getPatchNumber() + " encountered an error :" + e.getMessage());
            repo.notify(NotificationParameters.builder().patchNumber(patch.getPatchNumber()).errorNotification(setupParams.getErrorNotification()).build());
        }
    }
}
