package com.apgsga.microservice.patch.core.impl.jenkins;

import com.apgsga.artifact.query.ArtifactDependencyResolver;
import com.apgsga.artifact.query.ArtifactManager;
import com.apgsga.microservice.patch.api.*;
import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@SuppressWarnings("unused")
@Profile("live")
@Component("jenkinsPipelinePreprocessor")
public class JenkinsPipelinePreprocessor {

    @SuppressWarnings("unused")
    protected static final Log LOGGER = LogFactory.getLog(JenkinsPipelinePreprocessor.class.getName());


    public static final String ENTWICKLUNG_STAGE = "entwicklung";

    @SuppressWarnings("unused")
    @Autowired
    @Qualifier("patchPersistence")
    private PatchPersistence backend;

    @SuppressWarnings("unused")
    @Autowired
    private ArtifactManager am;

    @SuppressWarnings("unused")
    @Autowired
    private ArtifactDependencyResolver dependencyResolver;


    public String retrieveStagesTargetAsCSV() {
        StringBuilder stagesAsCSV = new StringBuilder();
        for (StageMapping sm : backend.stageMappings().getStageMappings()) {
            if (!sm.getName().equalsIgnoreCase(ENTWICKLUNG_STAGE)) {
                stagesAsCSV.append(sm.getName()).append(",");
            }
        }
        return stagesAsCSV.substring(0, stagesAsCSV.length() - 1);
    }

    /**
     * Patch is automated with ServiceMetaData, Dependency Level and Module Name for further processing in the Pipeline
     * @param bp Parameters for the Jenkins Pipelines
     */
    public void preProcessBuildPipeline(BuildParameter bp) {
        Patch patch = backend.findById(bp.getPatchNumber());
        List<Service> services = Lists.newArrayList();
        for (Service service : patch.getServices()) {
            ServiceMetaData serviceMetaData = backend.getServiceMetaDataByName(service.getServiceName());
            List<MavenArtifact> artifactsToPatch = service.getArtifactsToPatch();
            dependencyResolver.resolveDependencies(service.getArtifactsToPatch());
            for (MavenArtifact mavenArtifact : artifactsToPatch) {
                String artifactName = am.getArtifactName(mavenArtifact.getGroupId(), mavenArtifact.getArtifactId(), mavenArtifact.getVersion());
                mavenArtifact.withName(artifactName);
            }
            services.add(service.toBuilder().serviceMetaData(serviceMetaData).build());
        }
        backend.savePatch(patch.toBuilder().services(services).build());

    }


    public String retrieveTargetForStageName(String stageName) {
        return backend.targetFor(stageName);
    }

    public Patch retrievePatch(String patchNumber) {
        return backend.findById(patchNumber);
    }
}
