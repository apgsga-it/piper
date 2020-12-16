package com.apgsga.artifact.query.impl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.transfer.ArtifactNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.apgsga.artifact.query.ArtifactManager;
import com.apgsga.artifact.query.RepositorySystemFactory;
import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.SearchCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

public class ArtifactManagerImpl implements ArtifactManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactManagerImpl.class);

    private static final Map<SearchCondition, String> templateMap = Maps.newConcurrentMap();

    static {
        templateMap.put(SearchCondition.FORMS2JAVA, "classpath:templateForms2Java.json");
        templateMap.put(SearchCondition.PERSISTENT, "classpath:templatePersistence.json");
        templateMap.put(SearchCondition.IT21UI, "classpath:templateIt21Ui.json");
    }

    private final RepositorySystemFactory systemFactory;

    private final RepositorySystem system;

    private final RepositorySystemSession session;

    private final String localRepo;

    public ArtifactManagerImpl(String localRepo, RepositorySystemFactory systemFactory) {
        super();
        this.systemFactory = systemFactory;
        this.system = systemFactory.newRepositorySystem();
        this.session = systemFactory.newRepositorySystemSession(system, localRepo);
        this.localRepo = localRepo;
    }

    @Override
    public void cleanLocalMavenRepo() {
        LOGGER.info("About to clean Local Maven Repo");
        cleanLocalMavenRepo("com/affichage");
        cleanLocalMavenRepo("com/apgsga");
    }

    private void cleanLocalMavenRepo(String path) {
        LOGGER.info("About to clean Local Maven Repo");
        final ResourceLoader rl = new FileSystemResourceLoader();
        Resource resource = rl.getResource(localRepo + "/" + path);
        try {
            Path rootPath = Paths.get(resource.getURI());
            Files.walk(rootPath).sorted(Comparator.reverseOrder()).forEach(f -> delete(rootPath, f));
            LOGGER.info("Done cleaning Local Maven Repo");
        } catch (IOException e) {
            LOGGER.error("File : " + localRepo + " couldn't be deleted", e);
            LOGGER.error(ExceptionUtils.getFullStackTrace(e));
        }
    }

    public static void delete(Path root, Path p) {
        try {
            if (!root.equals(p)) {
                Files.delete(p);
                LOGGER.info("Deleted: {} ", p.toAbsolutePath());
            }
        } catch (IOException e) {
            LOGGER.error("File : " + p.toFile().getAbsolutePath() + " couldn't be deleted", e);
            LOGGER.error(ExceptionUtils.getFullStackTrace(e));
        }
    }


    private Model loadPomModel(MavenArtifact bomCoordinates) {
        org.eclipse.aether.artifact.Artifact bom = load(bomCoordinates);
        Preconditions.checkNotNull(bom, String.format("Bom couldn't be loaded %s", bomCoordinates.toString()));
        FileReader fileReader = null;
        try {
            MavenXpp3Reader mavenReader = new MavenXpp3Reader();
            File bomFile = bom.getFile();
            fileReader = new FileReader(bomFile);
            return mavenReader.read(fileReader);
        } catch (IOException | XmlPullParserException e) {
            LOGGER.error("Error Loading Bom Model", e);
            LOGGER.error(ExceptionUtils.getFullStackTrace(e));
            throw ExceptionFactory.create("Bom couldn't be parsed %s",e, bomCoordinates.toString());
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    LOGGER.error("Error Closing File Reader from loading Bom Model", e);
                    LOGGER.error(ExceptionUtils.getFullStackTrace(e));
                }
            }
        }
    }

    private Artifact load(MavenArtifact bomCoord) {
        Artifact artifact = new DefaultArtifact(bomCoord.getGroupId(), bomCoord.getArtifactId(), "pom", bomCoord.getVersion());
        ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setArtifact(artifact);
        artifactRequest.setRepositories(this.systemFactory.newRepositories());
        try {
            ArtifactResult artifactResult = system.resolveArtifact(session, artifactRequest);
            artifact = artifactResult.getArtifact();
            return artifact;
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof ArtifactNotFoundException) {
                LOGGER.warn("Artifact not found", cause);
                return null;
            }
            throw ExceptionFactory.create("Exception Loading pom: %s",e,bomCoord.toString());
        }
    }

    @Override
    public List<MavenArtifact> getAllDependencies(MavenArtifact bom) {
        return getAllDependencies(bom, SearchCondition.APPLICATION);
    }

    @Override
    public List<MavenArtifact> getAllDependencies(MavenArtifact bomCoordinates, SearchCondition searchFilter) {
        return getArtifactsWithVersionFromBom(bomCoordinates, searchFilter);
    }

    private List<MavenArtifact> getArtifactsWithVersionFromBom(MavenArtifact bomCoordinates, SearchCondition searchFilter) {
        Model model = loadPomModel(bomCoordinates);
        final List<MavenArtifact> artifacts = getArtifacts(model);
        Properties properties = model.getProperties();
        normalizeVersions(artifacts, properties);
        if (searchFilter.equals(SearchCondition.ALL)) {
            return artifacts;
        } else if (searchFilter.equals(SearchCondition.APPLICATION)) {
            return artifacts.stream()
                    .filter(artifact -> (artifact.getGroupId().startsWith("com.apgsga")
                            || artifact.getGroupId().startsWith("com.affichage"))
                            && artifact.getVersion().endsWith("SNAPSHOT"))
                    .collect(Collectors.toList());
        } else {
            if (templateMap.containsKey(searchFilter)) {
                ResourceLoader rl = new FileSystemResourceLoader();
                Resource resource = rl.getResource(templateMap.get(searchFilter));
                ObjectMapper mapper = new ObjectMapper();
                try {
                    MavenArtifact[] template = mapper.readValue(resource.getInputStream(), MavenArtifact[].class);
                    List<MavenArtifact> templateList = Arrays.asList(template);
                    return artifacts.stream().filter(art -> filter(templateList,art)).collect(Collectors.toList());
                } catch (IOException e) {
                    throw ExceptionFactory.create("Exception Loading pom:  %s with searchFilter: %s",e,bomCoordinates.toString(), searchFilter.toString());
                }
            }
            return Collections.emptyList();
        }
    }


    private boolean filter(List<MavenArtifact> templateList, MavenArtifact art) {
        return templateList.stream().anyMatch(o -> o.getArtifactId().equals(art.getArtifactId()) &&  o.getGroupId().equals(art.getGroupId()));
    }


    private static List<MavenArtifact> getArtifacts(Model model) {
        DependencyManagement dependencyManagement = model.getDependencyManagement();
        List<Dependency> dmDeps = dependencyManagement.getDependencies();
        return dmDeps.stream().map(ArtifactManagerImpl::create).collect(Collectors.toList());
    }

    public static MavenArtifact create(Dependency dependency) {
        return MavenArtifact.builder()
                .artifactId(dependency.getArtifactId())
                .groupId(dependency.getGroupId())
                .version(dependency.getVersion()).build();
    }

    private static void normalizeVersions(List<MavenArtifact> artifacts, Properties properties) {
        for (MavenArtifact artifact : artifacts) {
            String version = artifact.getVersion();
            if (version.startsWith("${") && version.endsWith("}")) {
                normalizeVersion(artifact, version, properties);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static void normalizeVersion(MavenArtifact artifact, String version, Properties properties) {
        String key = version.substring(2, version.length() - 1);
        String value = properties.getProperty(key);
        if (value == null) {
            String errMsg = String.format("Artefact : %s  has null Version reference : %s, ignored",
                    artifact.toString(), version);
            LOGGER.warn(errMsg);
        }
        if (value.startsWith("${") && value.endsWith("}")) {
            normalizeVersion(artifact, value, properties);
        }
        artifact.withVersion(value);
    }

    @Override
    public String getArtifactName(String groupId, String artifactId, String version) {
        Model model = loadPomModel(MavenArtifact.builder().artifactId(artifactId).groupId(groupId).version(version).build());
        return model.getName();
    }

}
