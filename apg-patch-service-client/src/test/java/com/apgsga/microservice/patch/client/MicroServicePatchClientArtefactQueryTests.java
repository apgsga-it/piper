package com.apgsga.microservice.patch.client;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileCopyUtils;

import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.api.SearchCondition;
import com.apgsga.microservice.patch.client.config.MicroServicePatchClientConfig;
import com.apgsga.microservice.patch.server.MicroPatchServer;
import com.apgsga.microservice.patch.server.impl.persistence.FilebasedPatchPersistence;

@RunWith(SpringRunner.class)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = { MicroPatchServer.class,
		MicroServicePatchClientConfig.class })
@TestPropertySource(locations = "application-test.properties")
@ActiveProfiles("test,mock,mavenRepo,groovyactions")
public class MicroServicePatchClientArtefactQueryTests {

	private MicroservicePatchClient patchClient;

	@Autowired
	@Qualifier("patchPersistence")
	private PatchPersistence repo;

	@Value("${json.db.location:target/testdb}")
	private String dbLocation;

	@Value("${json.db.work.location:work}")
	private String dbWorkLocation;

	@Value("${local.server.port}")
	private String localPort;

	@Before
	public void setUp() {
		patchClient = new MicroservicePatchClient("localhost:" + localPort);
		final ResourceLoader rl = new FileSystemResourceLoader();
		Resource testResources = rl.getResource("src/test/resources/json");
		Resource workDir = rl.getResource(dbWorkLocation);
		final PatchPersistence per = new FilebasedPatchPersistence(testResources, workDir);
		Patch testPatch5401 = per.findById("5401");
		Patch testPatch5402 = per.findById("5402");
		repo.clean();

		try {
			File persistSt = new File(dbLocation);
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/ServicesMetaData.json"),
					new File(persistSt, "ServicesMetaData.json"));
		} catch (IOException e) {
			Assert.fail("Unable to copy ServicesMetaData.json test file into testDb folder : " + e.getMessage());
		}

		repo.savePatch(testPatch5401);
		repo.savePatch(testPatch5402);
	}

	@Test
	public void testFindArtefacts() {
		Patch result = patchClient.findById("5401");
		Assert.assertNotNull(result);
		List<MavenArtifact> mavenArtefacts = patchClient.listMavenArtifacts(result);
		Assert.assertTrue(mavenArtefacts.size() > 0);
		Assert.assertTrue(mavenArtefacts.stream().allMatch(new Predicate<MavenArtifact>() {

			@Override
			public boolean test(MavenArtifact t) {
				return t.getGroupId().startsWith("com.apgsga") || t.getGroupId().startsWith("com.affichage");
			}
		}));
	}

	@Test
	public void testFindArtefactsDefault() {
		Patch result = patchClient.findById("5401");
		Assert.assertNotNull(result);
		List<MavenArtifact> mavenArtefacts = patchClient.listMavenArtifacts(result, SearchCondition.APPLICATION);
		Assert.assertTrue(mavenArtefacts.size() > 0);
		Assert.assertTrue(mavenArtefacts.stream().allMatch(new Predicate<MavenArtifact>() {

			@Override
			public boolean test(MavenArtifact t) {
				return t.getGroupId().startsWith("com.apgsga") || t.getGroupId().startsWith("com.affichage");
			}
		}));
	}
	
	@Test
	public void testFindArtefactsAll() {
		Patch result = patchClient.findById("5401");
		Assert.assertNotNull(result);
		List<MavenArtifact> mavenArtefacts = patchClient.listMavenArtifacts(result, SearchCondition.ALL);
		Assert.assertTrue(mavenArtefacts.size() > 0);
		Assert.assertTrue(mavenArtefacts.stream().anyMatch(new Predicate<MavenArtifact>() {

			@Override
			public boolean test(MavenArtifact t) {
				return t.getGroupId().startsWith("com.apgsga") || t.getGroupId().startsWith("com.affichage");
			}
		}));
		Assert.assertTrue(mavenArtefacts.stream().anyMatch(new Predicate<MavenArtifact>() {

			@Override
			public boolean test(MavenArtifact t) {
				return !t.getGroupId().startsWith("com.apgsga") &&  !t.getGroupId().startsWith("com.affichage");
			}
		}));
	}

}
