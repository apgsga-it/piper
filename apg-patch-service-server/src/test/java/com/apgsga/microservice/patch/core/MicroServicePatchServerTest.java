package com.apgsga.microservice.patch.core;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

import com.apgsga.microservice.patch.api.*;
import com.apgsga.microservice.patch.server.MicroPatchServer;
import com.google.common.collect.Maps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.apgsga.microservice.patch.exceptions.PatchServiceRuntimeException;
import com.apgsga.microservice.patch.core.impl.PatchActionExecutor;
import com.apgsga.microservice.patch.core.impl.PatchActionExecutorFactory;
import com.apgsga.microservice.patch.core.impl.SimplePatchContainerBean;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@TestPropertySource(locations = "application-test.properties")
@SpringBootTest(classes = MicroPatchServer.class)
@ActiveProfiles("test,mock,mockMavenRepo,groovyactions")
public class MicroServicePatchServerTest {

	protected final Log LOGGER = LogFactory.getLog(getClass());

	@Value("${json.meta.info.db.location}")
	private String metaInfoDb;

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
		final ResourceLoader rl = new FileSystemResourceLoader();
		Resource testResources = rl.getResource("src/test/resources/json");
		try {
			File persistSt = new File(metaInfoDb);
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/ServicesMetaData.json"), new File(persistSt, "ServicesMetaData.json"));
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/OnDemandTargets.json"), new File(persistSt, "OnDemandTargets.json"));
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/StageMappings.json"), new File(persistSt, "StageMappings.json"));
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/TargetInstances.json"), new File(persistSt, "TargetInstances.json"));
		} catch (IOException e) {
			Assert.fail("Unable to copy JSON test files into testDb folder");
		}
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
		Service service = Service.create().serviceName("It21ui").microServiceBranch(("SomeBaseBranch"));
		service.addMavenArtifacts(new MavenArtifact("ArtifactId1", "GroupId1", "Version1"));
		service.addMavenArtifacts(new MavenArtifact("ArtifactId2", "GroupId2", "Version2"));
		patch.addServices(service);
		patch.setDbPatchBranch("SomePatchBranch");
		patch.addDbObjects(new DbObject("FileName1", "FilePath1"));
		patch.addDbObjects(new DbObject("FileName2", "FilePath2"));
		patchService.save(patch);
		Patch result = patchService.findById("SomeUnqiueNumber3");
		assertNotNull(result);
		assertEquals(patch, result);
	}

	@Test
	public void testPatchActionEntwicklungInstallationsbereit() {
		Patch patch = new Patch();
		patch.setPatchNummer("SomeUnqiueNumber3");
		Service service = Service.create().serviceName("It21ui").microServiceBranch(("SomeBaseBranch"));
		service.addMavenArtifacts(new MavenArtifact("ArtifactId1", "GroupId1", "Version1"));
		service.addMavenArtifacts(new MavenArtifact("ArtifactId2", "GroupId2", "Version2"));
		patchService.save(patch);
		PatchActionExecutor patchActionExecutor = patchActionFactory.create(patchService);
		patchActionExecutor.execute("SomeUnqiueNumber3", "EntwicklungInstallationsbereit");
	}

	@Test
	public void testPatchPipelineInputAction() {
		Patch patch = new Patch();
		patch.setPatchNummer("SomeUnqiueNumber3");
		Service service = Service.create().serviceName("It21ui").microServiceBranch(("SomeBaseBranch"));
		service.addMavenArtifacts(new MavenArtifact("ArtifactId1", "GroupId1", "Version1"));
		service.addMavenArtifacts(new MavenArtifact("ArtifactId2", "GroupId2", "Version2"));
		patchService.save(patch);
		PatchActionExecutor patchActionExecutor = patchActionFactory.create(patchService);
		patchActionExecutor.execute("SomeUnqiueNumber3", "InformatiktestInstallationsbereit");
	}

	@Test
	public void testPatchCancelAction() {
		Patch patch = new Patch();
		patch.setPatchNummer("SomeUnqiueNumber3");
		Service service = Service.create().serviceName("It21ui").microServiceBranch(("SomeBaseBranch"));
		service.addMavenArtifacts(new MavenArtifact("ArtifactId1", "GroupId1", "Version1"));
		service.addMavenArtifacts(new MavenArtifact("ArtifactId2", "GroupId2", "Version2"));
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
		Service service1 = Service.create().serviceName("It21ui1").microServiceBranch(("SomeBaseBranch1"));
		Service service2 = Service.create().serviceName("It21ui2").microServiceBranch(("SomeBaseBranch2"));
		MavenArtifact ma1 = new MavenArtifact("test-ma1", "com.apgsga", "1.0");
		MavenArtifact ma2 = new MavenArtifact("test-ma2", "com.apgsga", "1.0");
		MavenArtifact ma3 = new MavenArtifact("test-ma3", "com.apgsga", "1.0");
		DbObject db1 = new DbObject("test-db1", "com.apgsga.ch/sql/db/test-db1");
		db1.setModuleName("test-db1");
		DbObject db2 = new DbObject("test-db2", "com.apgsga.ch/sql/db/test-db2");
		db2.setModuleName("test-db2");		
		p1.addDbObjects(db1);
		p1.addDbObjects(db2);
		service1.addMavenArtifacts(ma1);
		service2.addMavenArtifacts(ma2);
		service1.addMavenArtifacts(ma3);
		service2.addMavenArtifacts(ma3);
		p1.addServices(service1);
		p2.addServices(service2);
		patchService.save(p1);
		patchService.save(p2);
		assertEquals(2, patchService.findById("p1").getService("It21ui1").getMavenArtifacts().size());
		assertEquals(2, patchService.findById("p2").getService("It21ui2").getMavenArtifacts().size());
		assertEquals(1, patchService.findWithObjectName("ma1").size());
		assertEquals(1, patchService.findWithObjectName("ma2").size());
		assertEquals(2, patchService.findWithObjectName("ma3").size());
		assertEquals(0, patchService.findWithObjectName("wrongName").size());
		assertTrue(patchService.findWithObjectName("test-db2").size() == 1);
	}

	@Test
	public void testAssembleAndDeployStartPipeline() {
		Map<String,String> params = Maps.newHashMap();
		String target = "chei212";
		patchService.startAssembleAndDeployPipeline(target);
	}

	@Test
	public void testStartInstallPipeline() {
		Map<String,String> params = Maps.newHashMap();
		String target = "chei212";
		patchService.startInstallPipeline(target);
	}

	@Test
	public void testListOnDemandTargets() {
		List<String> onDemandTargets = patchService.listOnDemandTargets();
		assertEquals(4,onDemandTargets.size());
		assertTrue(onDemandTargets.contains("DEV-CHEI212"));
		assertTrue(onDemandTargets.contains("DEV-CHEI211"));
		assertTrue(onDemandTargets.contains("DEV-CM"));
		assertTrue(onDemandTargets.contains("DEV-JHE"));
	}

	@Test
	// JHE (17.08.2020) : Ignoring it since it requires DB pre-requisite to work
	//					  Also the following properties have to be correctly defined into application-test.properties
	//							rdbms.oracle.url
	//							rdbms.oracle.user.name
	//							rdbms.oracle.user.pwd
	@Ignore
	public void testExecuteStateTransitionActionInDb() {
		patchService.executeStateTransitionActionInDb("7018",0L);
	}

	@Test
	// JHE (17.08.2020) : Ignoring it since it requires DB pre-requisite to work
	//					  Also the following properties have to be correctly defined into application-test.properties
	//							rdbms.oracle.url
	//							rdbms.oracle.user.name
	//							rdbms.oracle.user.pwd
	@Ignore
	public void testPatchIdsForStatus() {
		List<String> patchIds = patchService.patchIdsForStatus("0");
		Assert.assertEquals(45,patchIds.size());
	}

	@Test
	// JHE (19.08.2020) : Ignoring it since it requires DB pre-requisite to work
	//					  Also the following properties have to be correctly defined into application-test.properties
	//							rdbms.oracle.url
	//							rdbms.oracle.user.name
	//							rdbms.oracle.user.pwd
	@Ignore
	public void testCopyPatchFile() {
		Patch p1 = new Patch();
		p1.setPatchNummer("6201");
		Patch p2 = new Patch();
		p2.setPatchNummer("6202");
		patchService.save(p1);
		patchService.save(p2);
		String destFolder = System.getProperty("java.io.tmpdir");
		Map params = Maps.newHashMap();
		params.put("status", "Anwendertest");
		params.put("destFolder", destFolder);
		patchService.copyPatchFiles(params);

	}

}
