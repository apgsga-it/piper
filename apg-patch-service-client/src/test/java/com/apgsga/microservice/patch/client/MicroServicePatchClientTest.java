package com.apgsga.microservice.patch.client;

import com.apgsga.microservice.patch.api.*;
import com.apgsga.microservice.patch.client.config.MicroServicePatchClientConfig;
import com.apgsga.microservice.patch.core.impl.persistence.FilebasedPatchPersistence;
import com.apgsga.microservice.patch.server.MicroPatchServer;
import com.google.common.collect.Lists;
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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

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

	@Value("${json.meta.info.db.location}")
	private String metaInfoDbLocation;
	
	@Value("${local.server.port}")
	private String localPort;

	@Before
	public void setUp() throws IOException {
		patchClient = new MicroservicePatchClient("localhost:" + localPort);
		final ResourceLoader rl = new FileSystemResourceLoader();
		Resource testResources = rl.getResource("src/test/resources/json");
		Resource workDir = rl.getResource(dbWorkLocation);
		final PatchPersistence per = new FilebasedPatchPersistence(testResources, workDir);
		Patch testPatch5401 = per.findById("5401");
		Patch testPatch5402 = per.findById("5402");
		repo.clean();

		try {
			File persistSt = new File(metaInfoDbLocation);
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/ServicesMetaData.json"),new File(persistSt, "ServicesMetaData.json"));
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/OnDemandTargets.json"),new File(persistSt, "OnDemandTargets.json"));
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/StageMappings.json"),new File(persistSt, "StageMappings.json"));
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/TargetInstances.json"),new File(persistSt, "TargetInstances.json"));
		} catch (IOException e) {
			fail("Unable to copy ServicesMetaData.json test file into testDb folder : " + e.getMessage());
		}

		repo.savePatch(testPatch5401);
		repo.savePatch(testPatch5402);
		PatchLogDetails pld = new PatchLogDetails();
		pld.setTarget("dev-jhe");
		pld.setPatchPipelineTask("Build");
		pld.setLogText("Started");
		pld.setDateTime(new Date());
		repo.savePatchLog("5401",pld);
	}

	@Test
	public void testSaveEmptyWithId() {
		Patch patch = new Patch();
		patch.setPatchNummer("SomeUnqiueNumber1");
		patchClient.save(patch);
		Patch result = patchClient.findById("SomeUnqiueNumber1");
		assertNotNull(result);
		assertEquals(patch, result);
	}
	
	@Test
	public void testSavePatchLog() {
		Patch p = new Patch();
		p.setPatchNummer("anotherUniqueId");
		patchClient.save(p);
		PatchLogDetails pld = new PatchLogDetails();
		pld.setTarget("dev-jhe");
		pld.setPatchPipelineTask("Build");
		pld.setLogText("Started");
		pld.setDateTime(new Date());
		try {
			patchClient.log("anotherUniqueId",pld);
			fail();
		}
		catch(UnsupportedOperationException ex) {
			assertEquals(ex.getMessage(), "Logging patch activity not supported");
		}
	}

	@Test
	public void testSaveEmptyWithIdAndRemove() {
		Patch patch = new Patch();
		patch.setPatchNummer("SomeUnqiueNumber2");
		patchClient.save(patch);
		Patch result = patchClient.findById("SomeUnqiueNumber2");
		assertNotNull(result);
		assertEquals(patch, result);
		patchClient.remove(result);
		result = patchClient.findById("SomeUnqiueNumber2");
		assertNull(result);
	}

	@Test
	public void testSaveEmptyWithOutId() {
		Patch patch = new Patch();
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
		Patch patch = new Patch();
		patch.setPatchNummer("SomeUnqiueNumber3");
		Service service = Service.create().serviceName("It21Ui").microServiceBranch("SomeBaseBranch");
		patch.addServices(service);
		patch.setDbPatchBranch("SomePatchBranch");
		patch.addDbObjects(new DbObject("FileName1", "FilePath1"));
		patch.addDbObjects(new DbObject("FileName2", "FilePath2"));
		service.addMavenArtifacts(MavenArtifact.create().artifactId("ArtifactId1").groupId( "GroupId1").version("SomeVersion1"));
		service.addMavenArtifacts(MavenArtifact.create().artifactId("ArtifactId2").groupId( "GroupId2").version("SomeVersion2"));
		patchClient.save(patch);
		Patch result = patchClient.findById("SomeUnqiueNumber3");
		assertNotNull(result);
		assertEquals(patch, result);
	}
	
	@Test
	public void testFindByIds() {
		List<Patch> patches = patchClient.findByIds(Lists.newArrayList("5401","5402"));
		assertEquals(2, patches.size());
	}
	
	@Test
	public void testFindPatchLogById() {
		PatchLog pl = patchClient.findPatchLogById("5401");
		assertNotNull(pl);
	}
	
	@Test
	public void testFindWithObjectName() { 		
		Patch p1 = new Patch();
		p1.setPatchNummer("p1");
		Patch p2 = new Patch();
		p2.setPatchNummer("p2");
		patchClient.save(p1);
		patchClient.save(p2);
		Service service1 = Service.create().serviceName("It21Ui").microServiceBranch("SomeBaseBranch1");
		Service service2 = Service.create().serviceName("It21Ui").microServiceBranch("SomeBaseBranch2");
		assertNotNull(patchClient.findById("p1"));
		assertNotNull(patchClient.findById("p2"));
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
		patchClient.save(p1);
		patchClient.save(p2);
		assertTrue(patchClient.findById("p1").getMavenArtifacts().size() == 2);
		assertTrue(patchClient.findById("p2").getMavenArtifacts().size() == 2);
		assertTrue(patchClient.findWithObjectName("ma1").size() == 1);
		assertTrue(patchClient.findWithObjectName("ma2").size() == 1);
		assertTrue(patchClient.findWithObjectName("ma3").size() == 2);
		assertTrue(patchClient.findWithObjectName("wrongName").size() == 0);
		assertTrue(patchClient.findWithObjectName("test-db2").size() == 1);
	}

	@Test
	public void testListOnDemandTargets() {
		List<String> onDemandTargets = patchClient.listOnDemandTargets();
		assertEquals(4,onDemandTargets.size());
		assertTrue(onDemandTargets.contains("DEV-CHEI212"));
		assertTrue(onDemandTargets.contains("DEV-CHEI211"));
		assertTrue(onDemandTargets.contains("DEV-CM"));
		assertTrue(onDemandTargets.contains("DEV-JHE"));
	}

	@Test
	public void testStageMappingsWithinPatch() {
		Patch patch = new Patch();
		patch.setPatchNummer("2222");
		patchClient.save(patch);
		Patch result = patchClient.findById("2222");
		List<StageMapping> stagesMapping = result.getStagesMapping();
		stagesMapping.get(0).getName().equals("Entwicklung");
		stagesMapping.get(1).getName().equals("Informatiktest");
		stagesMapping.get(2).getName().equals("Anwendertest");
		stagesMapping.get(3).getName().equals("Produktion");
	}
}
