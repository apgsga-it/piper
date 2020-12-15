package com.apgsga.microservice.patch.core;

import com.apgsga.microservice.patch.api.*;
import com.apgsga.microservice.patch.api.Package;
import com.apgsga.microservice.patch.core.impl.SimplePatchContainerBean;
import com.apgsga.microservice.patch.exceptions.PatchServiceRuntimeException;
import com.apgsga.microservice.patch.server.MicroPatchServer;
import com.google.common.collect.Lists;
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
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

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
		Patch patch = Patch.builder().patchNumber("SomeUnqiueNumber1").build();
		patchService.save(patch);
		Patch result = patchService.findById("SomeUnqiueNumber1");
		assertNotNull(result);
		assertEquals(patch, result);
	}
	
	@Test
	public void testSavePatchLogWithoutCorrespondingPatch() {
		try {
			PatchLogDetails pld = PatchLogDetails.builder().build();
			patchService.log("SomeUnqiueNumber1",pld);
			fail();
		} catch(PatchServiceRuntimeException e) {
			LOGGER.info(e.toString());
			assertEquals("SimplePatchContainerBean.log.patch.not.exist", e.getMessageKey());
		}
	}
	
	@Test
	public void testSavePatchLogWithOneDetail() {
		String patchNumber = "someUniqueNum1";
		Patch patch = Patch.builder().patchNumber(patchNumber).build();
		patchService.save(patch);
		PatchLogDetails pld = PatchLogDetails.builder().target("dev-jhe").patchPipelineTask("Build").datetime(new Date()).build();
		patchService.log(patch.getPatchNumber(),pld);
		PatchLog result = patchService.findPatchLogById(patchNumber);
		assertNotNull(result);
		assertTrue(result.getLogDetails().size() == 1);
	}
	
	@Test
	public void testSavePatchLogWithSeveralDetail() {
		PatchLogDetails pld = PatchLogDetails.builder().target("dev-jhe").patchPipelineTask("Build").datetime(new Date()).build();
		PatchLogDetails pld2 = PatchLogDetails.builder().target("dev-jhe").patchPipelineTask("Build 2").datetime(new Date()).build();
		PatchLogDetails pld3 = PatchLogDetails.builder().target("dev-jhe").patchPipelineTask("Build 3").datetime(new Date()).build();
		String patchNumber = "notEmpty1";
		Patch p = Patch.builder().patchNumber(patchNumber).build();
		patchService.save(p);
		patchService.log(p.getPatchNumber(),pld);
		PatchLog result = patchService.findPatchLogById(patchNumber);
		assertNotNull(result);
		assertTrue(result.getLogDetails().size() == 1);
		patchService.save(p);
		patchService.log(p.getPatchNumber(),pld2);
		patchService.save(p);
		patchService.log(p.getPatchNumber(),pld3);
		result = patchService.findPatchLogById(patchNumber);
		assertTrue(result.getLogDetails().size() == 3);
	}

	@Test
	public void testSaveEmptyWithIdAndRemove() {
		Patch patch = Patch.builder().patchNumber("SomeUnqiueNumber2").build();
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
		final MavenArtifact.MavenArtifactBuilder builder = MavenArtifact.builder();
		final MavenArtifact bomData = builder
				.artifactId("bomArtifactid")
				.groupId("bomGroupid")
				.name("whatevername")
				.build();
		final Package pkgData = Package.builder()
				.packagerName("packagermodulename")
				.starterCoordinates(Lists.newArrayList("someappgroupId:artifactId", "anotherappgroupId:anotherartifactId"))
				.build();
		ServiceMetaData serviciceMetaData = ServiceMetaData.builder()
				.bomCoordinates(bomData)
				.baseVersionNumber("aBaseVersionNumber")
				.microServiceBranch("SomeBaseBranch")
				.packages(Lists.newArrayList(pkgData))
				.revisionMnemoPart("revpart")
				.serviceName("It21Ui").build();
		Service service = Service.builder()
				.serviceName("It21Ui")
				.artifactsToPatch(Lists.newArrayList(MavenArtifact.builder()
								.artifactId("ArtifactId1")
								.groupId("GroupId1")
								.version("SomeVersion1")
								.name("somecvsmodulename1").build(),
						MavenArtifact.builder()
								.artifactId("ArtifactId2")
								.groupId("GroupId2")
								.version("SomeVersion2")
								.name("somecvsmodulename2").build()
				))
				.serviceMetaData(serviciceMetaData).build();
		Patch patch = Patch.builder().patchNumber("SomeUnqiueNumber3")
				.dbPatchBranch("SomePatchBranch")
				.services(Lists.newArrayList(service))
				.dbObjects(Lists.newArrayList(
						DbObject.builder()
								.fileName("FileName1")
								.filePath("FilePath1").build(),
						DbObject.builder()
								.fileName("FileName2")
								.filePath("FilePath2").build()
				)).build();
		patchService.save(patch);
		Patch result = patchService.findById("SomeUnqiueNumber3");
		assertNotNull(result);
		assertEquals(patch, result);
	}

	@Test
	public void testBuildPatch() {
		Patch p = Patch.builder().patchNumber("2222").build();
		patchService.save(p);
		try {
			BuildParameter bp = BuildParameter.builder().patchNumber("2222").stageName("Informatiktest").successNotification("success").errorNotification("error").build();
			patchService.build(bp);
		}
		catch(Exception e) {
			LOGGER.error(repo.toString());
			fail("An error occured while testing the build of a patch");
		}
	}

	@Test
	public void testFindWithObjectName() {
		Patch p1 = Patch.builder().patchNumber("p1").build();
		Patch p2 = Patch.builder().patchNumber("p2").build();
		patchService.save(p1);
		patchService.save(p2);
		assertNotNull(patchService.findById("p1"));
		assertNotNull(patchService.findById("p2"));
		List<Service> services = Lists.newArrayList(Service.builder()
						.serviceName("It21Ui")
						.artifactsToPatch(Lists.newArrayList(MavenArtifact.builder()
										.artifactId("test-ma1")
										.groupId("com.apgsga")
										.version("1.0")
										.name("test-ma1").build(),
								MavenArtifact.builder()
										.artifactId("test-ma2")
										.groupId("com.apgsga")
										.version("1.0")
										.name("test-ma2").build(),
								MavenArtifact.builder()
										.artifactId("test-ma3")
										.groupId("com.apgsga")
										.version("1.0")
										.name("test-ma3").build()
						)).build(),
				Service.builder()
						.serviceName("SomeOtherApp")
						.artifactsToPatch(Lists.newArrayList(MavenArtifact.builder()
										.artifactId("test-ma4")
										.groupId("com.apgsga")
										.version("1.0")
										.name("test-ma4").build(),
								MavenArtifact.builder()
										.artifactId("test-ma5")
										.groupId("com.apgsga")
										.version("1.0")
										.name("test-ma5").build(),
								MavenArtifact.builder()
										.artifactId("test-ma6")
										.groupId("com.apgsga")
										.version("1.0")
										.name("test-ma6").build()
						)).build()
		);
		List<DbObject> dbObjects = Lists.newArrayList(  DbObject.builder()
						.fileName("test-db1")
						.filePath("com.apgsga.ch/sql/db/test-db1")
						.moduleName("test-db1")
						.build(),
				DbObject.builder()
						.fileName("test-db2")
						.filePath("com.apgsga.ch/sql/db/test-db2")
						.moduleName("test-db2")
						.build());
		Patch p1Updated = p1.toBuilder()
				.services(Lists.newArrayList(Service.builder()
						.serviceName("It21Ui")
						.artifactsToPatch(Lists.newArrayList(MavenArtifact.builder()
										.artifactId("test-ma1")
										.groupId("com.apgsga")
										.version("1.0")
										.name("test-ma1").build(),
								MavenArtifact.builder()
										.artifactId("test-ma3")
										.groupId("com.apgsga")
										.version("1.0")
										.name("test-ma3").build()
						)).build()))
				.dbObjects(Lists.newArrayList( DbObject.builder()
						.fileName("test-db1")
						.filePath("com.apgsga.ch/sql/db/test-db1")
						.moduleName("test-db1")
						.build()))
				.build();
		Patch p2Updated = p2.toBuilder()
				.services(Lists.newArrayList(Service.builder()
						.serviceName("SomeOtherService")
						.artifactsToPatch(Lists.newArrayList(MavenArtifact.builder()
										.artifactId("test-ma3")
										.groupId("com.apgsga")
										.version("1.0")
										.name("test-ma3").build(),
								MavenArtifact.builder()
										.artifactId("test-ma2")
										.groupId("com.apgsga")
										.version("1.0")
										.name("test-ma2").build()

						)).build()))
				.dbObjects(Lists.newArrayList( DbObject.builder()
						.fileName("test-db2")
						.filePath("com.apgsga.ch/sql/db/test-db2")
						.moduleName("test-db2")
						.build()))
				.build();
		patchService.save(p1Updated);
		patchService.save(p2Updated);
		assertTrue(patchService.findById("p1").retrieveAllArtifactsToPatch().size() == 2);
		assertTrue(patchService.findById("p2").retrieveAllArtifactsToPatch().size() == 2);
		assertTrue(patchService.findWithObjectName("ma1").size() == 1);
		assertTrue(patchService.findWithObjectName("ma2").size() == 1);
		assertTrue(patchService.findWithObjectName("ma3").size() == 2);
		assertTrue(patchService.findWithObjectName("wrongName").size() == 0);
		assertTrue(patchService.findWithObjectName("test-db2").size() == 1);
	}

	@Test
	public void testAssembleAndDeployStartPipelineForNonExistingPatch() {
		Patch p = new Patch();
		p.setPatchNummer("5401");
		Patch p2 = new Patch();
		p2.setPatchNummer("5402");
		patchService.save(p);
		patchService.save(p2);
		AssembleAndDeployParameters params = AssembleAndDeployParameters.create()
											.target("DEV-JHE")
											.errorNotification("error")
											.successNotification("success")
											.addPatchNumber("5401")
											.addPatchNumber("5402")
											.addGradlePackageProjectAsVcsPath("testPkg");
		patchService.startAssembleAndDeployPipeline(params);
	}

	@Test
	public void testAssembleAndDeployStartPipelineForExistingPatch() {
		Patch p = new Patch();
		p.setPatchNummer("5401");
		Patch p2 = new Patch();
		p2.setPatchNummer("5402");
		patchService.save(p);
		patchService.save(p2);
		AssembleAndDeployParameters params = AssembleAndDeployParameters.create()
					.target("DEV-JHE")
					.errorNotification("error")
					.successNotification("success")
					.addPatchNumber("5401")
					.addPatchNumber("5402")
					.addGradlePackageProjectAsVcsPath("testPkg");
		patchService.startAssembleAndDeployPipeline(params);
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




}
