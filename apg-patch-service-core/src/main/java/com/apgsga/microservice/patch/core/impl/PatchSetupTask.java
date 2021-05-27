package com.apgsga.microservice.patch.core.impl;

import com.apgsga.artifact.query.ArtifactDependencyResolver;
import com.apgsga.artifact.query.ArtifactManager;
import com.apgsga.microservice.patch.api.*;
import com.apgsga.microservice.patch.core.commands.CommandRunner;
import com.apgsga.microservice.patch.core.commands.docker.DockerTagAndPushCmd;
import com.apgsga.microservice.patch.core.commands.patch.vcs.PatchSshCommand;
import com.apgsga.microservice.patch.exceptions.Asserts;
import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.stream.Collectors;

public class PatchSetupTask implements Runnable {

    protected static final Log LOGGER = LogFactory.getLog(PatchSetupTask.class.getName());

    public static Runnable create(CommandRunner jschSession, CommandRunner localSession, Patch patch, PatchPersistence repo, SetupParameter setupParams, ArtifactManager am, ArtifactDependencyResolver dependencyResolver, String pathToDockerTagScript,String cvsConfigSpecificBranchFilePath) {
        return new PatchSetupTask(jschSession, localSession, patch, repo, setupParams, am, dependencyResolver,pathToDockerTagScript,cvsConfigSpecificBranchFilePath);
    }

    private final CommandRunner jschSession;
    private final CommandRunner localSession;
    private final Patch patch;
    private final PatchPersistence repo;
    private final SetupParameter setupParams;
    private final ArtifactManager am;
    private final ArtifactDependencyResolver dependencyResolver;
    private final String pathToDockerTagScript;
    private final String cvsConfigSpecificBranchFilePath;

    private PatchSetupTask(CommandRunner jschSession, CommandRunner localSession, Patch patch, PatchPersistence repo, SetupParameter setupParams, ArtifactManager am, ArtifactDependencyResolver dependencyResolver, String pathToDockerTagScript, String cvsConfigSpecificBranchFilePath) {
        super();
        this.jschSession = jschSession;
        this.localSession = localSession;
        this.patch = patch;
        this.repo = repo;
        this.setupParams = setupParams;
        this.am = am;
        this.dependencyResolver = dependencyResolver;
        this.pathToDockerTagScript = pathToDockerTagScript;
        this.cvsConfigSpecificBranchFilePath = cvsConfigSpecificBranchFilePath;
    }

    @Override
    public void run() {
        LOGGER.info("Patch Setup Task started for patch " + patch.getPatchNumber());
        resolveDependencies();
        preprocess();
        if (!patch.getDbPatch().retrieveDbObjectsAsVcsPath().isEmpty()) {
            tagDbModules();
        }
        if (!patch.getDockerServices().isEmpty()) {
            tagAndPushDockerServices();
        }
        for (Service service : patch.getServices()) {
            if (!service.retrieveMavenArtifactsAsVcsPath().isEmpty()) {
                createTagFor(service);
            }
        }
        postProcess();
    }

    private void postProcess() {
        jschSession.postProcess();
        repo.savePatch(patch);
        LOGGER.info("Patch Setup Task for patch " + patch.getPatchNumber() + " DONE !! -> notifying db accordingly!");
        notifyDbFor(setupParams.getSuccessNotification());
    }

    private void preprocess() {
        patch.nextTagNr();
        jschSession.preProcess();
    }

    private void tagAndPushDockerServices() {
        try {
            LOGGER.info("Following Docker services will be tagged for Patch " + patch.getPatchNumber() + " : " + patch.getDockerServices());
            localSession.run(new DockerTagAndPushCmd(pathToDockerTagScript, patch.getDockerServices(), patch.getPatchNumber()));
        } catch (Exception e) {
            LOGGER.error("Patch Setup Task for patch " + patch.getPatchNumber() + " encountered an error while tagging and pushing Docker Service(s):" + e.getMessage());
            notifyDbFor(setupParams.getErrorNotification());
            throw e;
        }
    }

    private void tagDbModules() {
        try {
            LOGGER.info("Creating Tag for DB Objects for patch " + patch.getPatchNumber());
            DBPatch dbPatch = patch.getDbPatch();
            dbPatch.withPatchTag(patch.getTagNr());
            jschSession.run(PatchSshCommand.createTagPatchModulesCmd(dbPatch.getPatchTag(), patch.getDbPatch().getDbPatchBranch(),
                    patch.getDbPatch().retrieveDbObjectsAsVcsPath()));
        } catch (Exception e) {
            LOGGER.error("Patch Setup Task for patch " + patch.getPatchNumber() + " encountered an error while tagging DB Modules:" + e.getMessage());
            notifyDbFor(setupParams.getErrorNotification());
            throw e;
        }
    }

    private void createTagFor(Service service) {
        try {
            LOGGER.info("Creating Tag for Java Artifact for patch " + patch.getPatchNumber() + " and service " + service.getServiceName());
            ServiceMetaData serviceMetaData = repo.getServiceMetaDataByName(service.getServiceName());
            service.withServiceMetaData(serviceMetaData);
            service.withPatchTag(patch.getTagNr(), patch.getPatchNumber());
            jschSession.run(PatchSshCommand.createTagPatchModulesCmd(service.getPatchTag(), serviceMetaData.getMicroServiceBranch(), service.retrieveMavenArtifactsAsVcsPath()));
        } catch (Exception e) {
            LOGGER.error("Patch Setup Task for patch " + patch.getPatchNumber() + " encountered an error while tagging Java Artifacts:" + e.getMessage());
            notifyDbFor(setupParams.getErrorNotification());
            throw e;
        }
    }

    private void notifyDbFor(String notification) {
        repo.notify(NotificationParameters.builder().patchNumbers(patch.getPatchNumber()).notification(notification).build());
        LOGGER.info("DB has been notified for patch " + patch.getPatchNumber() + " with following notification : " + notification);
    }

    private void resolveDependencies() {
        for (Service service : patch.getServices()) {
            ServiceMetaData serviceMetaData = repo.getServiceMetaDataByName(service.getServiceName());
            List<MavenArtifact> artifactsToBuild = service.retrieveMavenArtifactsToBuild();
            dependencyResolver.resolveDependencies(service.getArtifactsToPatch());
            for (MavenArtifact mavenArtifact : artifactsToBuild) {
                String artifactName = am.getArtifactName(mavenArtifact.getGroupId(), mavenArtifact.getArtifactId(), mavenArtifact.getVersion());
                Asserts.notNull(artifactName,"Missing artifactname for mavenArtifactId: %s, groupId: %s", mavenArtifact.getArtifactId(), mavenArtifact.getGroupId());
                mavenArtifact.withName(artifactName);
            }
            service.withServiceMetaData(serviceMetaData);
        }
    }
}
