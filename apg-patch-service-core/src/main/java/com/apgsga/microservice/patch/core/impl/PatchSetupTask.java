package com.apgsga.microservice.patch.core.impl;

import com.apgsga.artifact.query.ArtifactDependencyResolver;
import com.apgsga.artifact.query.ArtifactManager;
import com.apgsga.microservice.patch.api.*;
import com.apgsga.microservice.patch.core.commands.CommandRunner;
import com.apgsga.microservice.patch.core.commands.patch.vcs.PatchSshCommand;
import com.google.common.collect.Lists;
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
            Patch patchWithResDep = resolveDependencies();
            Integer lastTagNr = patchWithResDep.getTagNr();
            Integer nextTagNr = lastTagNr + 1;
            String patchBranch = patchWithResDep.getDbPatchBranch();
            String nextPatchTag = patchBranch + "_" + nextTagNr.toString();
            LOGGER.info("Patch Setup Task started for patch " + patchWithResDep.getPatchNumber());
            jschSession.preProcess();
            if (!patchWithResDep.retrieveDbObjectsAsVcsPath().isEmpty()) {
                LOGGER.info("Creating Tag for DB Objects for patch " + patchWithResDep.getPatchNumber());
                jschSession.run(PatchSshCommand.createTagPatchModulesCmd(nextPatchTag, patchWithResDep.getDbPatchBranch(),
                        patchWithResDep.retrieveDbObjectsAsVcsPath()));
            }
            for (Service service : patchWithResDep.getServices()) {
                if (!service.retrieveMavenArtifactsAsVcsPath().isEmpty()) {
                    LOGGER.info("Creating Tag for Java Artifact for patch " + patchWithResDep.getPatchNumber() + " and service " + service.getServiceName());
                    ServiceMetaData serviceMetaData = repo.getServiceMetaDataByName(service.getServiceName());
                    jschSession.run(PatchSshCommand.createTagPatchModulesCmd(nextPatchTag, serviceMetaData.getMicroServiceBranch(),
                            service.retrieveMavenArtifactsAsVcsPath()));
                }
            }
            jschSession.postProcess();
            repo.savePatch(patchWithResDep.toBuilder().patchTag(nextPatchTag).tagNr(nextTagNr).build());
            LOGGER.info("Patch Setup Task started for patch " + patchWithResDep.getPatchNumber() + " DONE !! -> notifying db accordingly!");
            repo.notify(NotificationParameters.builder().patchNumber(patchWithResDep.getPatchNumber()).successNotification(setupParams.getSuccessNotification()).build());
        } catch (Exception e) {
            LOGGER.error("Patch Setup Task for patch " + patch.getPatchNumber() + " encountered an error :" + e.getMessage());
            repo.notify(NotificationParameters.builder().patchNumber(patch.getPatchNumber()).errorNotification(setupParams.getErrorNotification()).build());
        }
    }

    // JHE : This has to be done here because we need the artifactName in order to correctly TAG in CVS. Also, when installing via onDemand Job, the dependencyResolver
    //       must first be ran.
    private Patch resolveDependencies() {
        List<Service> services = Lists.newArrayList();
        for (Service service : patch.getServices()) {
            ServiceMetaData serviceMetaData = repo.getServiceMetaDataByName(service.getServiceName());
            List<MavenArtifact> artifactsToPatch = service.getArtifactsToPatch();
            dependencyResolver.resolveDependencies(service.getArtifactsToPatch());
            for (MavenArtifact mavenArtifact : artifactsToPatch) {
                String artifactName = am.getArtifactName(mavenArtifact.getGroupId(), mavenArtifact.getArtifactId(), mavenArtifact.getVersion());
                mavenArtifact.withName(artifactName);
            }
            services.add(service.toBuilder().serviceMetaData(serviceMetaData).build());
        }
        return patch.toBuilder().services(services).build();
    }
}
