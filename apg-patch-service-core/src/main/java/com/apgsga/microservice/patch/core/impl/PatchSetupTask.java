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

    public static Runnable create(CommandRunner jschSession, CommandRunner localSession, Patch patch, PatchPersistence repo, SetupParameter setupParams, ArtifactManager am, ArtifactDependencyResolver dependencyResolver, String pathToDockerTagScript) {
        return new PatchSetupTask(jschSession, localSession, patch, repo, setupParams, am, dependencyResolver,pathToDockerTagScript);
    }

    private final CommandRunner jschSession;
    private final CommandRunner localSession;
    private final Patch patch;
    private final PatchPersistence repo;
    private final SetupParameter setupParams;
    private final ArtifactManager am;
    private final ArtifactDependencyResolver dependencyResolver;
    private final String pathToDockerTagScript;

    private PatchSetupTask(CommandRunner jschSession, CommandRunner localSession, Patch patch, PatchPersistence repo, SetupParameter setupParams, ArtifactManager am, ArtifactDependencyResolver dependencyResolver, String pathToDockerTagScript) {
        super();
        this.jschSession = jschSession;
        this.localSession = localSession;
        this.patch = patch;
        this.repo = repo;
        this.setupParams = setupParams;
        this.am = am;
        this.dependencyResolver = dependencyResolver;
        this.pathToDockerTagScript = pathToDockerTagScript;
    }

    @Override
    public void run() {
        try {
            resolveDependencies();
            patch.nextTagNr();
            LOGGER.info("Patch Setup Task started for patch " + patch.getPatchNumber());
            jschSession.preProcess();
            if (!patch.getDbPatch().retrieveDbObjectsAsVcsPath().isEmpty()) {
                LOGGER.info("Creating Tag for DB Objects for patch " + patch.getPatchNumber());
                DBPatch dbPatch = patch.getDbPatch();
                dbPatch.withPatchTag(patch.getTagNr());
                jschSession.run(PatchSshCommand.createTagPatchModulesCmd(dbPatch.getPatchTag(), patch.getDbPatch().getDbPatchBranch(),
                        patch.getDbPatch().retrieveDbObjectsAsVcsPath()));
            }
            if (!patch.getDockerServices().isEmpty()) {
                LOGGER.info("Following Docker services will be tagged for Patch " + patch.getPatchNumber() + " : " + patch.getDockerServices());
                localSession.run(new DockerTagAndPushCmd(pathToDockerTagScript,patch.getDockerServices(),patch.getPatchNumber()));
            }
            for (Service service : patch.getServices()) {
                if (!service.retrieveMavenArtifactsAsVcsPath().isEmpty()) {
                    // JHE (26.05.2021): This is a temporary workaround because Digiflex is using Artifacts coming from different CVS Branches.
                    //                   These Artifacts will become libraries for which we'll then be able to use a fix version, without the need of tagging anymore.
                    //                   In between, so that we can go forward with Digiflex integration, we use this workaround.
                    if(service.getServiceName().equalsIgnoreCase("digiflex")) {
                        tagForDigiflex(service);
                    }
                    else {
                        LOGGER.info("Creating Tag for Java Artifact for patch " + patch.getPatchNumber() + " and service " + service.getServiceName());
                        ServiceMetaData serviceMetaData = repo.getServiceMetaDataByName(service.getServiceName());
                        service.withServiceMetaData(serviceMetaData);
                        service.withPatchTag(patch.getTagNr(), patch.getPatchNumber());
                        jschSession.run(PatchSshCommand.createTagPatchModulesCmd(service.getPatchTag(), serviceMetaData.getMicroServiceBranch(),
                                service.retrieveMavenArtifactsAsVcsPath()));
                    }
                }
            }
            jschSession.postProcess();
            repo.savePatch(patch);
            LOGGER.info("Patch Setup Task started for patch " + patch.getPatchNumber() + " DONE !! -> notifying db accordingly!");
            repo.notify(NotificationParameters.builder().patchNumbers(patch.getPatchNumber()).notification(setupParams.getSuccessNotification()).build());
        } catch (Exception e) {
            LOGGER.error("Patch Setup Task for patch " + patch.getPatchNumber() + " encountered an error :" + e.getMessage());
            repo.notify(NotificationParameters.builder().patchNumbers(patch.getPatchNumber()).notification(setupParams.getErrorNotification()).build());
            throw e;
        }
    }

    // JHE (26.05.2021) : Temporary workaround, see comment from caller for further details
    private void tagForDigiflex(Service service) {
        LOGGER.info("Creating Tag for Java Artifacts for Digiflex using a temporary workaround.");
        ServiceMetaData serviceMetaData = repo.getServiceMetaDataByName(service.getServiceName());
        ServiceMetaData it21ServiceMetaData = repo.getServiceMetaDataByName("it21");
        service.withPatchTag(patch.getTagNr(), patch.getPatchNumber());

        final List<String> artifactsToBeBuildFromIt21Branch = Lists.newArrayList("com.affichage.it21.domainwerte.ds","com.affichage.it21.domainwerte.pm","com.affichage.it21.domainwerte.vk","com.affichage.it21.domainwerte.lo","com.affichage.it21.common.dao");
        LOGGER.info("List of Digiflex Artifacts which have to be tagged from IT21 Branch: " + artifactsToBeBuildFromIt21Branch);
        List<String> mavenArtifactsOnDigiflexBranch = service.retrieveMavenArtifactsAsVcsPath().stream().filter(art -> !artifactsToBeBuildFromIt21Branch.contains(art)).collect(Collectors.toList());
        List<String> mavenArtifactsOnIt21Branch = service.retrieveMavenArtifactsAsVcsPath().stream().filter(art -> artifactsToBeBuildFromIt21Branch.contains(art)).collect(Collectors.toList());

        if(!mavenArtifactsOnDigiflexBranch.isEmpty()) {
            LOGGER.info("Tagging will be done from Digiflex Branch for following Artifacts : " + mavenArtifactsOnDigiflexBranch);
            service.withServiceMetaData(serviceMetaData);
            jschSession.run(PatchSshCommand.createTagPatchModulesCmd(service.getPatchTag(), serviceMetaData.getMicroServiceBranch(),mavenArtifactsOnDigiflexBranch));
        }
        if(!mavenArtifactsOnIt21Branch.isEmpty()) {
            LOGGER.info("Tagging will be done from IT21 Branch (for Digiflex Service) for following Artifacts : " + mavenArtifactsOnIt21Branch);
            service.withServiceMetaData(it21ServiceMetaData);
            jschSession.run(PatchSshCommand.createTagPatchModulesCmd(service.getPatchTag(), it21ServiceMetaData.getMicroServiceBranch(),mavenArtifactsOnIt21Branch));
        }

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
