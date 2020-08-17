package com.apgsga.system.mapping.impl

import com.apgsga.system.mapping.api.TargetSystemMapping
import org.eclipse.jgit.api.Git
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import java.nio.file.Files
import java.nio.file.Paths

@Component
class TargetSystemMappingImpl implements TargetSystemMapping {

    @Value('${github.repo.url}')
    private String githubRepoUrl

    @Value('${github.branch:master}')
    private String gitBranch

    @Value('${target.system.mapping.json.file.path}')
    private String targetSystemMappingJsonFilePath

    public static final String TARGET_SYSTEM_MAPPING_GROOVY_SCRIPT_PATH = "resources/targetSystemMappings.groovy"

    // Dynmically built for each new Spring component
    private String cloneDirectoryPath

    // Will contain TargetSystemMapping script loaded from Github
    private def tsmObject

    @PostConstruct
    private void init() {
        UUID uuid = UUID.randomUUID();
        cloneDirectoryPath = System.getProperty("java.io.tmpdir") + uuid.toString()
        loadGroovyScript()
    }

    @PreDestroy
    private void clean() {
        // JHE (14.08.2020): I would have used common-io, but we were encountering locking problem on .git folder.
        //                   Only way which worked was to recursively iterate on all files.
        deleteDirectory(new File(cloneDirectoryPath))
    }

    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    private void loadGroovyScript() {
        cloneGitRepo()
        def gs = new GroovyShell()
        def script = gs.parse(new File("${cloneDirectoryPath}/${TARGET_SYSTEM_MAPPING_GROOVY_SCRIPT_PATH}"))
        tsmObject = script.create(targetSystemMappingJsonFilePath)
    }

    private void cloneGitRepo() {
        if(!Files.exists(Paths.get(cloneDirectoryPath))) {
            Git.cloneRepository()
                    .setURI(githubRepoUrl)
                    .setDirectory(Paths.get(cloneDirectoryPath).toFile())
                    .setBranch(gitBranch)
                    .call()
                    .close()
        }
    }

    @Override
    Integer findStatus(String toStatus) {
        return tsmObject.findStatus(toStatus)
    }

    @Override
    String serviceTypeFor(String serviceName, String target) {
        return tsmObject.serviceTypeFor(serviceName,target)
    }

    @Override
    String installTargetFor(String serviceName, String target) {
        return tsmObject.installTargetFor(serviceName,target)
    }

    @Override
    boolean isLightInstance(String target) {
        return tsmObject.isLightInstance(target)
    }

    @Override
    List<String> validToStates() {
        return tsmObject.validToStates()
    }

    @Override
    List<String> listInstallTargets() {
        return tsmObject.listInstallTargets()
    }

    @Override
    Map<String, Map<String, String>> stateMap() {
        return tsmObject.stateMap()
    }
}
