package com.apgsga.microservice.patch.client;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

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

import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.api.impl.DbObjectBean;
import com.apgsga.microservice.patch.api.impl.MavenArtifactBean;
import com.apgsga.microservice.patch.api.impl.PatchBean;
import com.apgsga.microservice.patch.client.config.MicroServicePatchClientConfig;
import com.apgsga.microservice.patch.server.MicroPatchServer;
import com.apgsga.microservice.patch.server.impl.persistence.FilebasedPatchPersistence;

@RunWith(SpringRunner.class)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = { MicroPatchServer.class,
		MicroServicePatchClientConfig.class })
@TestPropertySource(locations = "application-test.properties")
@ActiveProfiles("test,mock,groovyactions")
public class MicroServicePatchClientTest {

	@Autowired
	private MicroservicePatchClient patchClient;

	@Autowired
	@Qualifier("patchPersistence")
	private PatchPersistence repo;

	@Value("${json.db.location:target/testdb}")
	private String dbLocation;
	
	@Value("${json.db.work.location:work}")
	private String dbWorkLocation;

	@Before
	public void setUp() {
		final ResourceLoader rl = new FileSystemResourceLoader();
		Resource testResources = rl.getResource("src/test/resources/json");
		Resource workDir = rl.getResource(dbWorkLocation);
		final PatchPersistence per = new FilebasedPatchPersistence(testResources, workDir);
		Patch testPatch5401 = per.findById("5401");
		Patch testPatch5402 = per.findById("5402");
		repo.clean();

		try {
			File persistSt = new File(dbLocation);
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/ServiceData.json"),
					new File(persistSt, "ServiceData.json"));
		} catch (IOException e) {
			Assert.fail("Unable to copy ServiceData.json test file into testDb folder");
		}

		repo.savePatch(testPatch5401);
		repo.savePatch(testPatch5402);
	}

	@Test
	public void testSaveEmptyWithId() {
		Patch patch = new PatchBean();
		patch.setPatchNummer("SomeUnqiueNumber1");
		patchClient.save(patch);
		Patch result = patchClient.findById("SomeUnqiueNumber1");
		Assert.assertNotNull(result);
		Assert.assertEquals(patch, result);
	}

	@Test
	public void testSaveEmptyWithIdAndRemove() {
		Patch patch = new PatchBean();
		patch.setPatchNummer("SomeUnqiueNumber2");
		patchClient.save(patch);
		Patch result = patchClient.findById("SomeUnqiueNumber2");
		Assert.assertNotNull(result);
		Assert.assertEquals(patch, result);
		patchClient.remove(result);
		result = patchClient.findById("SomeUnqiueNumber2");
		Assert.assertNull(result);
	}

	@Test
	public void testSaveEmptyWithOutId() {
		Patch patch = new PatchBean();
		try {
			patchClient.save(patch);
			fail();
		} catch (Throwable e) {
			// TODO Detail , Exception Handling
			// Ok
		}
	}

	@Test
	public void testSaveWithArtifacts() {
		Patch patch = new PatchBean();
		patch.setPatchNummer("SomeUnqiueNumber3");
		patch.setServiceName("It21ui");
		patch.setMicroServiceBranch("SomeBaseBranch");
		patch.setDbPatchBranch("SomePatchBranch");
		patch.addDbObjects(new DbObjectBean("FileName1", "FilePath1"));
		patch.addDbObjects(new DbObjectBean("FileName2", "FilePath2"));
		patch.addMavenArtifacts(new MavenArtifactBean("ArtifactId1", "GroupId1", "SomeVersion1"));
		patch.addMavenArtifacts(new MavenArtifactBean("ArtifactId2", "GroupId2", "SomeVersion2"));
		patchClient.save(patch);
		Patch result = patchClient.findById("SomeUnqiueNumber3");
		Assert.assertNotNull(result);
		Assert.assertEquals(patch, result);
	}

}
