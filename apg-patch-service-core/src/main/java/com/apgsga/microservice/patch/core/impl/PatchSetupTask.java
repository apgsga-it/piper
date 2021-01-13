package com.apgsga.microservice.patch.core.impl;

import com.apgsga.artifact.query.ArtifactDependencyResolver;
import com.apgsga.artifact.query.ArtifactManager;
import com.apgsga.microservice.patch.api.*;
import com.apgsga.microservice.patch.core.commands.CommandRunner;
import com.apgsga.microservice.patch.core.commands.patch.vcs.PatchSshCommand;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public class PatchSetupTask implements Runnable {

    protected static final Log LOGGER = LogFactory.getLog(PatchSetupTask.class.getName());

    public static Runnable create(CommandRunner jschSession, Patch patch, PatchPersistence repo, SetupParameter setupParams, ArtifactManager am, ArtifactDependencyResolver dependencyResolver) {
        return new PatchSetupTask(jschSession, patch, repo, setupParams, am, dependencyResolver);
    }

    private final CommandRunner jschSession;
    private final Patch patch;
    private final PatchPersistence repo;
    private final SetupParameter setupParams;
    private final ArtifactManager am;
    private final ArtifactDependencyResolver dependencyResolver;

    private PatchSetupTask(CommandRunner jschSession, Patch patch, PatchPersistence repo, SetupParameter setupParams, ArtifactManager am, ArtifactDependencyResolver dependencyResolver) {
        super();
        this.jschSession = jschSession;
        this.patch = patch;
        this.repo = repo;
        this.setupParams = setupParams;
        this.am = am;
        this.dependencyResolver = dependencyResolver;
    }

    @Override
    public void run() {
        try {
            resolveDependencies();
            Integer lastTagNr = patch.getTagNr();
            Integer nextTagNr = lastTagNr + 1;
            patch.withTagNr(nextTagNr);
            LOGGER.info("Patch Setup Task started for patch " + patch.getPatchNumber());
            jschSession.preProcess();
            if (!patch.getDbPatch().retrieveDbObjectsAsVcsPath().isEmpty()) {
                LOGGER.info("Creating Tag for DB Objects for patch " + patch.getPatchNumber());
                DBPatch dbPatch = patch.getDbPatch();
                dbPatch.withPatchTag(nextTagNr);
                jschSession.run(PatchSshCommand.createTagPatchModulesCmd(dbPatch.getPatchTag(), patch.getDbPatch().getDbPatchBranch(),
                        patch.getDbPatch().retrieveDbObjectsAsVcsPath()));
            }
            for (Service service : patch.getServices()) {
                if (!service.retrieveMavenArtifactsAsVcsPath().isEmpty()) {
                    LOGGER.info("Creating Tag for Java Artifact for patch " + patch.getPatchNumber() + " and service " + service.getServiceName());
                    ServiceMetaData serviceMetaData = repo.getServiceMetaDataByName(service.getServiceName());
                    service.withServiceMetaData(serviceMetaData);
                    service.withPatchTag(nextTagNr);
                    jschSession.run(PatchSshCommand.createTagPatchModulesCmd(service.getPatchTag(), serviceMetaData.getMicroServiceBranch(),
                            service.retrieveMavenArtifactsAsVcsPath()));
                }
            }
            jschSession.postProcess();
            repo.savePatch(patch);
            LOGGER.info("Patch Setup Task started for patch " + patch.getPatchNumber() + " DONE !! -> notifying db accordingly!");
            repo.notify(NotificationParameters.builder().patchNumber(patch.getPatchNumber()).successNotification(setupParams.getSuccessNotification()).build());
        } catch (Exception e) {
            LOGGER.error("Patch Setup Task for patch " + patch.getPatchNumber() + " encountered an error :" + e.getMessage());
            repo.notify(NotificationParameters.builder().patchNumber(patch.getPatchNumber()).errorNotification(setupParams.getErrorNotification()).build());
        }
    }

    private void resolveDependencies() {
        for (Service service : patch.getServices()) {
            ServiceMetaData serviceMetaData = repo.getServiceMetaDataByName(service.getServiceName());
            List<MavenArtifact> artifactsToPatch = service.getArtifactsToPatch();
            dependencyResolver.resolveDependencies(service.getArtifactsToPatch());
            for (MavenArtifact mavenArtifact : artifactsToPatch) {
                String artifactName = am.getArtifactName(mavenArtifact.getGroupId(), mavenArtifact.getArtifactId(), mavenArtifact.getVersion());
                mavenArtifact.withName(artifactName);
            }
            service.withServiceMetaData(serviceMetaData);
        }
    }
}
