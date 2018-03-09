package com.apgsga.artifact.query.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.apgsga.artifact.query.ArtifactManager;
import com.apgsga.artifact.query.impl.ArtifactManagerImpl;
import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.impl.MavenArtifactBean;
import com.google.common.collect.Lists;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource({ "classpath:test.properties" })
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class TestGettingDependenciesFromStarters {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TestGettingDependenciesFromStarters.class);

	@Configuration
	static class ContextConfiguration {

		@Value("${test.localRepo}")
		public String localRep;

		@Bean
		public ArtifactManager artifactManager() throws IOException {
			LOGGER.info("Running with MavenLocal from: " + localRep);
			return ArtifactManager.create(localRep);
		}
	}

	@Value("${test.starterproject.version:9.0.6.ADMIN-UIMIG-SNAPSHOT}")
	private String it21StarterVersion;

	@Value("${test.jadas.starter}")
	private String it21JadasAppStarterArtefactId;

	@Value("${test.it21app.starter}")
	private String it21UiAppStarterArtefactId;

	@Autowired
	private ArtifactManager am;

	@Test
	public void testExpectedStarterDependencies() throws DependencyResolutionException, ArtifactResolutionException,
			FileNotFoundException, IOException, XmlPullParserException {

		List<MavenArtifact> starterProjects = Lists.newArrayList();

		MavenArtifactBean it21MavenArtifactBean = new MavenArtifactBean(it21UiAppStarterArtefactId.split(":")[1],
				it21UiAppStarterArtefactId.split(":")[0], null);
		it21MavenArtifactBean.setName("it21ui-app-starter");
		starterProjects.add(it21MavenArtifactBean);
		MavenArtifactBean jadasMavenArtifactBean = new MavenArtifactBean(it21JadasAppStarterArtefactId.split(":")[1],
				it21JadasAppStarterArtefactId.split(":")[0], null);
		jadasMavenArtifactBean.setName("jadas-app-starter");
		starterProjects.add(jadasMavenArtifactBean);
		List<MavenArtifact> artifacts = am.getAllDependencies(starterProjects, it21StarterVersion);
		final ResourceLoader rl = new FileSystemResourceLoader();
		File file = rl.getResource("classpath:dependencyFromStarterProjects.txt").getFile();
		List<String> expected = com.google.common.io.Files.readLines(file, StandardCharsets.UTF_8).stream()
				.map(String::trim).collect(Collectors.toList());
		List<String> effective = artifacts.stream().map(m -> m.getGroupId() + ":" + m.getArtifactId())
				.collect(Collectors.toList());
		List<String> removedExcpected = Lists.newArrayList(effective);
		removedExcpected.removeAll(expected);
		Assert.assertEquals("Expected no diff, but got: " + removedExcpected.toString(), 0, removedExcpected.size());
		List<String> artifactNotIncluded = Lists.newArrayList("lieferschein-rep", "montageauftrag-rep",
				"lageretikette-rep", "vp-deckblatt-megaposter", "vp-offertverfall", "pz-service", "portal-service",
				"lo-reportservice");
		artifacts.stream().forEach(art -> {
			Assert.assertTrue(
					art.getGroupId().startsWith("com.affichage") || art.getGroupId().startsWith("com.apgsga"));
			Assert.assertFalse(art.getArtifactId().equals("it21ui-app-starter"));
			Assert.assertFalse(art.getArtifactId().equals("jadas-app-starter"));
			Assert.assertFalse(art.getArtifactId().equals("microservice-patch-common"));
			Assert.assertFalse(art.getArtifactId().equals("microservice-patch-server"));
			Assert.assertFalse(art.getArtifactId().equals("microservice-patch-client"));
			Assert.assertFalse(artifactNotIncluded.contains(art.getArtifactId()));
		});
	}
}