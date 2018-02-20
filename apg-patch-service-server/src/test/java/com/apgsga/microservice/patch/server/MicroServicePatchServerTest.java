package com.apgsga.microservice.patch.server;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.api.impl.DbObjectBean;
import com.apgsga.microservice.patch.api.impl.MavenArtifactBean;
import com.apgsga.microservice.patch.api.impl.PatchBean;
import com.apgsga.microservice.patch.server.impl.SimplePatchContainerBean;

@RunWith(SpringRunner.class)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@SpringBootTest(classes = MicroPatchServer.class)
@ActiveProfiles("test,mock")
public class MicroServicePatchServerTest {

	@Autowired
	private SimplePatchContainerBean patchService;

	@Autowired
	@Qualifier("patchPersistence")
	private PatchPersistence repo;

	@Before
	public void setUp() throws IOException {
		repo.clean();
	}

	@Test
	public void testSaveEmptyWithId() {
		Patch patch = new PatchBean();
		patch.setPatchNummer("SomeUnqiueNumber1");
		patchService.save(patch);
		Patch result = patchService.findById("SomeUnqiueNumber1");
		Assert.assertNotNull(result);
		Assert.assertEquals(patch, result);
	}

	@Test
	public void testSaveEmptyWithIdAndRemove() {
		Patch patch = new PatchBean();
		patch.setPatchNummer("SomeUnqiueNumber2");
		patchService.save(patch);
		Patch result = patchService.findById("SomeUnqiueNumber2");
		Assert.assertNotNull(result);
		Assert.assertEquals(patch, result);
		patchService.remove(result);
		result = patchService.findById("SomeUnqiueNumber2");
		Assert.assertNull(result);
	}

	@Test
	public void testSaveEmptyWithOutId() {
		Patch patch = new PatchBean();
		try {
			patchService.save(patch);
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
		patch.addMavenArtifacts(new MavenArtifactBean("ArtifactId1", "GroupId1", "Version1"));
		patch.addMavenArtifacts(new MavenArtifactBean("ArtifactId2", "GroupId2", "Version2"));
		patchService.save(patch);
		Patch result = patchService.findById("SomeUnqiueNumber3");
		Assert.assertNotNull(result);
		Assert.assertEquals(patch, result);
	}

}
