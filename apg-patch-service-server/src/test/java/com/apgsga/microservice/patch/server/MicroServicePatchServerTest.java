package com.apgsga.microservice.patch.server;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

import java.io.IOException;

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

import com.apgsga.microservice.patch.api.DbObject;
import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchLog;
import com.apgsga.microservice.patch.api.PatchPersistence;
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
	public void setUp()  {
		repo.clean();
	}

	@Test
	public void testSaveEmptyWithId() {
		Patch patch = new Patch();
		patch.setPatchNummer("SomeUnqiueNumber1");
		patchService.save(patch);
		Patch result = patchService.findById("SomeUnqiueNumber1");
		assertNotNull(result);
		assertEquals(patch, result);
	}
	
	@Test
	public void testSavePatchLogWithoutCorrespondingPatch() {
		try {
			Patch patch = new Patch();
			patch.setPatchNummer("SomeUnqiueNumber1");
			patchService.log(patch);
			fail();
		} catch(PatchServiceRuntimeException e) {
			LOGGER.info(e.toString());
			assertEquals("SimplePatchContainerBean.log.patchisnull", e.getMessageKey());
		}
	}
	
	@Test
	public void testSavePatchLogWithOneDetail() {
		String patchNumber = "someUniqueNum1";
		Patch p = new Patch();
		p.setPatchNummer(patchNumber);
		p.setCurrentTarget("chei211");
		p.setLogText("started");
		p.setCurrentPipelineTask("Build");
		patchService.save(p);
		patchService.log(p);
		PatchLog result = patchService.findPatchLogById(patchNumber);
		assertNotNull(result);
		assertTrue(result.getLogDetails().size() == 1);
	}
	
	@Test
	public void testSavePatchLogWithSeveralDetail() {
		String patchNumber = "notEmpty1";
		Patch p = new Patch();
		p.setPatchNummer(patchNumber);
		p.setCurrentTarget("chei211");
		p.setCurrentPipelineTask("Build");
		p.setLogText("started");
		patchService.save(p);
		patchService.log(p);
		PatchLog result = patchService.findPatchLogById(patchNumber);
		assertNotNull(result);
		assertTrue(result.getLogDetails().size() == 1);
		p.setCurrentPipelineTask("Build");
		p.setLogText("done");
		patchService.save(p);
		patchService.log(p);
		p.setCurrentPipelineTask("Installation");
		p.setLogText("started");
		patchService.save(p);
		patchService.log(p);
		result = patchService.findPatchLogById(patchNumber);
		assertTrue(result.getLogDetails().size() == 3);
	}

	@Test
	public void testSaveEmptyWithIdAndRemove() {
		Patch patch = new Patch();
		patch.setPatchNummer("SomeUnqiueNumber2");
		patchService.save(patch);
		Patch result = patchService.findById("SomeUnqiueNumber2");
		assertNotNull(result);
		assertEquals(patch, result);
		patchService.remove(result);
		result = patchService.findById("SomeUnqiueNumber2");
		assertNull(result);
	}

	@Test
	public void testSaveWithArtifacts() {
		Patch patch = new Patch();
		patch.setPatchNummer("SomeUnqiueNumber3");
		patch.setServiceName("It21ui");
		patch.setMicroServiceBranch("SomeBaseBranch");
		patch.setDbPatchBranch("SomePatchBranch");
		patch.addDbObjects(new DbObject("FileName1", "FilePath1"));
		patch.addDbObjects(new DbObject("FileName2", "FilePath2"));
		patch.addMavenArtifacts(new MavenArtifact("ArtifactId1", "GroupId1", "Version1"));
		patch.addMavenArtifacts(new MavenArtifact("ArtifactId2", "GroupId2", "Version2"));
		patchService.save(patch);
		Patch result = patchService.findById("SomeUnqiueNumber3");
		assertNotNull(result);
		assertEquals(patch, result);
	}

	@Test
	public void testPatchActionEntwicklungInstallationsbereit() {
		Patch patch = new Patch();
		patch.setPatchNummer("SomeUnqiueNumber3");
		patch.setServiceName("It21ui");
		patch.setMicroServiceBranch("SomeBaseBranch");
		patch.addMavenArtifacts(new MavenArtifact("ArtifactId1", "GroupId1", "Version1"));
		patch.addMavenArtifacts(new MavenArtifact("ArtifactId2", "GroupId2", "Version2"));
		patchService.save(patch);
		PatchActionExecutor patchActionExecutor = patchActionFactory.create(patchService);
		patchActionExecutor.execute("SomeUnqiueNumber3", "EntwicklungInstallationsbereit");
	}

	@Test
	public void testPatchPipelineInputAction() {
		Patch patch = new Patch();
		patch.setPatchNummer("SomeUnqiueNumber3");
		patch.setServiceName("It21ui");
		patch.setMicroServiceBranch("SomeBaseBranch");
		patch.addMavenArtifacts(new MavenArtifact("ArtifactId1", "GroupId1", "Version1"));
		patch.addMavenArtifacts(new MavenArtifact("ArtifactId2", "GroupId2", "Version2"));
		patchService.save(patch);
		PatchActionExecutor patchActionExecutor = patchActionFactory.create(patchService);
		patchActionExecutor.execute("SomeUnqiueNumber3", "InformatiktestInstallationsbereit");
	}

	@Test
	public void testPatchCancelAction() {
		Patch patch = new Patch();
		patch.setPatchNummer("SomeUnqiueNumber3");
		patch.setServiceName("It21ui");
		patch.setMicroServiceBranch("SomeBaseBranch");
		patch.addMavenArtifacts(new MavenArtifact("ArtifactId1", "GroupId1", "Version1"));
		patch.addMavenArtifacts(new MavenArtifact("ArtifactId2", "GroupId2", "Version2"));
		patchService.save(patch);
		PatchActionExecutor patchActionExecutor = patchActionFactory.create(patchService);
		patchActionExecutor.execute("SomeUnqiueNumber3", "Entwicklung");
	}
	
	@Test
	public void testFindWithObjectName() {
		Patch p1 = new Patch();
		p1.setPatchNummer("p1");
		Patch p2 = new Patch();
		p2.setPatchNummer("p2");
		patchService.save(p1);
		patchService.save(p2);
		assertNotNull(patchService.findById("p1"));
		assertNotNull(patchService.findById("p2"));
		MavenArtifact ma1 = new MavenArtifact("test-ma1", "com.apgsga", "1.0");
		MavenArtifact ma2 = new MavenArtifact("test-ma2", "com.apgsga", "1.0");
		MavenArtifact ma3 = new MavenArtifact("test-ma3", "com.apgsga", "1.0");
		DbObject db1 = new DbObject("test-db1", "com.apgsga.ch/sql/db/test-db1");
		db1.setModuleName("test-db1");
		DbObject db2 = new DbObject("test-db2", "com.apgsga.ch/sql/db/test-db2");
		db2.setModuleName("test-db2");		
		p1.addDbObjects(db1);
		p1.addDbObjects(db2);
		p1.addMavenArtifacts(ma1);
		p2.addMavenArtifacts(ma2);
		p1.addMavenArtifacts(ma3);
		p2.addMavenArtifacts(ma3);
		patchService.save(p1);
		patchService.save(p2);
		assertTrue(patchService.findById("p1").getMavenArtifacts().size() == 2);
		assertTrue(patchService.findById("p2").getMavenArtifacts().size() == 2);
		assertTrue(patchService.findWithObjectName("ma1").size() == 1);
		assertTrue(patchService.findWithObjectName("ma2").size() == 1);
		assertTrue(patchService.findWithObjectName("ma3").size() == 2);
		assertTrue(patchService.findWithObjectName("wrongName").size() == 0);
		assertTrue(patchService.findWithObjectName("test-db2").size() == 1);
	}

}
