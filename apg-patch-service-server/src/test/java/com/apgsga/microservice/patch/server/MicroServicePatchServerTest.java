package com.apgsga.microservice.patch.server;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchLog;
import com.apgsga.microservice.patch.api.PatchLogDetails;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.api.impl.DbObjectBean;
import com.apgsga.microservice.patch.api.impl.MavenArtifactBean;
import com.apgsga.microservice.patch.api.impl.PatchBean;
import com.apgsga.microservice.patch.api.impl.PatchLogBean;
import com.apgsga.microservice.patch.api.impl.PatchLogDetailsBean;
import com.apgsga.microservice.patch.exceptions.PatchServiceRuntimeException;
import com.apgsga.microservice.patch.server.impl.PatchActionExecutor;
import com.apgsga.microservice.patch.server.impl.PatchActionExecutorFactory;
import com.apgsga.microservice.patch.server.impl.SimplePatchContainerBean;

@RunWith(SpringRunner.class)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@TestPropertySource(locations = "application-test.properties")
@SpringBootTest(classes = MicroPatchServer.class)
@ActiveProfiles("test,mock,mockMavenRepo,groovyactions")
public class MicroServicePatchServerTest {

	protected final Log LOGGER = LogFactory.getLog(getClass());

	@Autowired
	private SimplePatchContainerBean patchService;

	@Autowired
	@Qualifier("patchPersistence")
	private PatchPersistence repo;

	@Autowired
	private PatchActionExecutorFactory patchActionFactory;

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
	public void testSavePatchLogWithoutCorrespondingPatch() {
		try {
			Patch patch = new PatchBean();
			patch.setPatchNummer("SomeUnqiueNumber1");
			patchService.log(patch);
			fail();
		} catch(PatchServiceRuntimeException e) {
			LOGGER.info(e.toString());
			Assert.assertEquals("SimplePatchContainerBean.log.patchisnull", e.getMessageKey());
		}
	}
	
	@Test
	public void testSavePatchLogWithOneDetail() {
		String patchNumber = "someUniqueNum1";
		Patch p = new PatchBean();
		p.setPatchNummer(patchNumber);
		p.setCurrentTarget("chei211");
		p.setStep("Build started");
		patchService.save(p);
		patchService.log(p);
		PatchLog result = patchService.findPatchLogById(patchNumber);
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getLogDetails().size() == 1);
	}
	
	@Test
	public void testSavePatchLogWithSeveralDetail() {
		String patchNumber = "notEmpty1";
		Patch p = new PatchBean();
		p.setPatchNummer(patchNumber);
		p.setCurrentTarget("chei211");
		p.setStep("Build started");
		patchService.save(p);
		patchService.log(p);
		PatchLog result = patchService.findPatchLogById(patchNumber);
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getLogDetails().size() == 1);
		p.setStep("Build done");
		patchService.save(p);
		patchService.log(p);
		p.setStep("Installation started");
		patchService.save(p);
		patchService.log(p);
		result = patchService.findPatchLogById(patchNumber);
		Assert.assertTrue(result.getLogDetails().size() == 3);
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

	@Test
	public void testPatchActionEntwicklungInstallationsbereit() {
		Patch patch = new PatchBean();
		patch.setPatchNummer("SomeUnqiueNumber3");
		patch.setServiceName("It21ui");
		patch.setMicroServiceBranch("SomeBaseBranch");
		patch.addMavenArtifacts(new MavenArtifactBean("ArtifactId1", "GroupId1", "Version1"));
		patch.addMavenArtifacts(new MavenArtifactBean("ArtifactId2", "GroupId2", "Version2"));
		patchService.save(patch);
		PatchActionExecutor patchActionExecutor = patchActionFactory.create(patchService);
		patchActionExecutor.execute("SomeUnqiueNumber3", "EntwicklungInstallationsbereit");
	}

	@Test
	public void testPatchPipelineInputAction() {
		Patch patch = new PatchBean();
		patch.setPatchNummer("SomeUnqiueNumber3");
		patch.setServiceName("It21ui");
		patch.setMicroServiceBranch("SomeBaseBranch");
		patch.addMavenArtifacts(new MavenArtifactBean("ArtifactId1", "GroupId1", "Version1"));
		patch.addMavenArtifacts(new MavenArtifactBean("ArtifactId2", "GroupId2", "Version2"));
		patchService.save(patch);
		PatchActionExecutor patchActionExecutor = patchActionFactory.create(patchService);
		patchActionExecutor.execute("SomeUnqiueNumber3", "InformatiktestInstallationsbereit");
	}

	@Test
	public void testPatchCancelAction() {
		Patch patch = new PatchBean();
		patch.setPatchNummer("SomeUnqiueNumber3");
		patch.setServiceName("It21ui");
		patch.setMicroServiceBranch("SomeBaseBranch");
		patch.addMavenArtifacts(new MavenArtifactBean("ArtifactId1", "GroupId1", "Version1"));
		patch.addMavenArtifacts(new MavenArtifactBean("ArtifactId2", "GroupId2", "Version2"));
		patchService.save(patch);
		PatchActionExecutor patchActionExecutor = patchActionFactory.create(patchService);
		patchActionExecutor.execute("SomeUnqiueNumber3", "Entwicklung");
	}

}
