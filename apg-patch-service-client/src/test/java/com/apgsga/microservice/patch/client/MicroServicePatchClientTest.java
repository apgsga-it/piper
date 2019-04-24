package com.apgsga.microservice.patch.client;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
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
import com.apgsga.microservice.patch.api.PatchLog;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.api.impl.DbObjectBean;
import com.apgsga.microservice.patch.api.impl.MavenArtifactBean;
import com.apgsga.microservice.patch.api.impl.PatchBean;
import com.apgsga.microservice.patch.api.impl.PatchLogBean;
import com.apgsga.microservice.patch.client.config.MicroServicePatchClientConfig;
import com.apgsga.microservice.patch.exceptions.PatchServiceRuntimeException;
import com.apgsga.microservice.patch.server.MicroPatchServer;
import com.apgsga.microservice.patch.server.impl.persistence.FilebasedPatchPersistence;
import com.google.common.collect.Lists;

@RunWith(SpringRunner.class)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = { MicroPatchServer.class,
		MicroServicePatchClientConfig.class })
@TestPropertySource(locations = "application-test.properties")
@ActiveProfiles("test,mock,mockMavenRepo,groovyactions")
public class MicroServicePatchClientTest {
	

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
		repo.savePatchLog(testPatch5401);
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
	public void testSavePatchLog() {
		Patch p = new PatchBean();
		p.setPatchNummer("anotherUniqueId");
		p.setCurrentTarget("chei212");
		p.setLogText("Build Started");
		patchClient.save(p);
		try {
			patchClient.log(p);
			fail();
		}
		catch(UnsupportedOperationException ex) {
			Assert.assertEquals(ex.getMessage(), "Logging patch activity not supported");
		}
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
	public void testSavePatchLogEmptyWithoutId() {
		Patch patch = new PatchBean();
		try {
			patchClient.log(patch);
			fail();
		} catch (Throwable e) {
			// TODO Detail , Exception Handling
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
	
	@Test
	public void testFindByIds() {
		List<Patch> patches = patchClient.findByIds(Lists.newArrayList("5401","5402"));
		Assert.assertEquals(2, patches.size());
	}
	
	@Test
	public void testFindPatchLogById() {
		PatchLog pl = patchClient.findPatchLogById("5401");
		Assert.assertNotNull(pl);
	}
	

}
