package com.apgsga.microservice.patch.core.impl;

import com.apgsga.artifact.query.ArtifactDependencyResolver;
import com.apgsga.artifact.query.ArtifactManager;
import com.apgsga.microservice.patch.api.*;
import com.apgsga.microservice.patch.core.commands.CommandRunner;
import com.apgsga.microservice.patch.core.commands.docker.DockerTagAndPushCmd;
import com.apgsga.microservice.patch.core.commands.patch.vcs.PatchSshCommand;
import com.apgsga.microservice.patch.exceptions.Asserts;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class PatchSetupTask implements Runnable {

    protected static final Log LOGGER = LogFactory.getLog(PatchSetupTask.class.getName());

    public static Runnable create(CommandRunner jschSession, CommandRunner localSession, Patch patch, PatchPersistence repo, SetupParameter setupParams, ArtifactManager am, ArtifactDependencyResolver dependencyResolver, String pathToDockerTagScript,String cvsConfigSpecificBranchFilePath, String tagDbModuleChunkSize) {
        return new PatchSetupTask(jschSession, localSession, patch, repo, setupParams, am, dependencyResolver,pathToDockerTagScript,cvsConfigSpecificBranchFilePath,tagDbModuleChunkSize);
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
    private final String tagDbModuleChunkSize;

    private PatchSetupTask(CommandRunner jschSession, CommandRunner localSession, Patch patch, PatchPersistence repo, SetupParameter setupParams, ArtifactManager am, ArtifactDependencyResolver dependencyResolver, String pathToDockerTagScript, String cvsConfigSpecificBranchFilePath, String tagDbModuleChunkSize) {
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
        this.tagDbModuleChunkSize = tagDbModuleChunkSize;
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
            List<List<String>> dbModulesChunks = Lists.partition(patch.getDbPatch().retrieveDbObjectsAsVcsPath(), Integer.parseInt(tagDbModuleChunkSize));
            LOGGER.info("DB Objects will be tagged in " + dbModulesChunks.size() + " chunk(s).");
            dbModulesChunks.forEach(chunk -> {
                LOGGER.info("Following DB Objects will now be tagged : " + chunk);
                jschSession.run(PatchSshCommand.createTagPatchModulesCmd(dbPatch.getPatchTag(), patch.getDbPatch().getDbPatchBranch(),chunk));
            });
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

            Map<String, List<String>> branchWithCvsModules = CvsModuleSplitter.create()
                    .withMavenArtifactsModuleNames(service.retrieveMavenArtifactsAsVcsPath())
                    .withDefaultCvsBranch(serviceMetaData.getMicroServiceBranch())
                    .withCvsConfigSpecificBranchFilePath(cvsConfigSpecificBranchFilePath)
                    .splitModuleForBranch();

            branchWithCvsModules.keySet().forEach(cvsBranch -> {
                LOGGER.info("Java Artifact " + service.getPatchTag() + " tag will be done from " + cvsBranch + " branch for following modules : " + branchWithCvsModules.get(cvsBranch));
                jschSession.run(PatchSshCommand.createTagPatchModulesCmd(service.getPatchTag(), cvsBranch, branchWithCvsModules.get(cvsBranch)));
            });
        } catch (Exception e) {
            LOGGER.error("Patch Setup Task for patch " + patch.getPatchNumber() + " encountered an error while tagging Java Artifacts:" + e.getMessage());
            notifyDbFor(setupParams.getErrorNotification());
        }
    }

    private Map<String, List<String>> splitModulesOnSpecificBranches(String defaultCvsBranchForService, List<String> p_mavenCvsModules) throws IOException {
        // Ensure we don't modify the original list
        List<String> mavenCvsModules = Lists.newArrayList(p_mavenCvsModules);
        Map<String, List<String>> result = Maps.newHashMap();
        // Specific branch configuration is define within cvsConfigSpecificBranchFilePath, if it exists
        if (Files.exists(new File(cvsConfigSpecificBranchFilePath).toPath())) {
            Properties branchConfig = new Properties();
            branchConfig.load(new FileInputStream(cvsConfigSpecificBranchFilePath));
            branchConfig.forEach((k,v) -> {
                List<String> modulesForSpecificBranch = Arrays.asList(v.toString().split(","));
                List<String> modulesForCurrentSpecificBranch = mavenCvsModules.stream().filter(m -> modulesForSpecificBranch.contains(m)).collect(Collectors.toList());
                if(!modulesForCurrentSpecificBranch.isEmpty()) {
                    result.put((String) k,modulesForCurrentSpecificBranch);
                    mavenCvsModules.removeIf(m -> modulesForCurrentSpecificBranch.contains(m));
                }
            });
            if(!mavenCvsModules.isEmpty()) {
                result.put(defaultCvsBranchForService,mavenCvsModules);
            }
        } else {
            result.put(defaultCvsBranchForService, mavenCvsModules);
        }
        return result;
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
