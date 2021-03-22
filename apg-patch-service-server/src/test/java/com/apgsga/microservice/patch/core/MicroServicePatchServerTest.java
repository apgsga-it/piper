package com.apgsga.microservice.patch.core;

import com.apgsga.microservice.patch.api.Package;
import com.apgsga.microservice.patch.api.*;
import com.apgsga.microservice.patch.core.impl.SimplePatchContainerBean;
import com.apgsga.microservice.patch.exceptions.PatchServiceRuntimeException;
import com.apgsga.microservice.patch.server.MicroPatchServer;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
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
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@TestPropertySource(locations = "application-test.properties")
@SpringBootTest(classes = MicroPatchServer.class)
@ActiveProfiles("test,mock,mockMavenRepo,patchOMock,mockDocker")
public class MicroServicePatchServerTest {

	protected final Log LOGGER = LogFactory.getLog(getClass());

	@Value("${json.meta.info.db.location}")
	private String metaInfoDb;

	@Value("${json.db.location:target/testdb}")
	private String dbLocation;

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
		Resource patchStorage = rl.getResource(dbLocation);
		Resource metaStorage = rl.getResource(metaInfoDb);
		repo.clean();
		try {
			File persistSt = metaStorage.getFile();
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/ServicesMetaData.json"), new File(persistSt, "ServicesMetaData.json"));
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/OnDemandTargets.json"), new File(persistSt, "OnDemandTargets.json"));
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/StageMappings.json"), new File(persistSt, "StageMappings.json"));
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/TargetInstances.json"), new File(persistSt, "TargetInstances.json"));
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/DbModules.json"), new File(persistSt, "DbModules.json"));

		} catch (IOException e) {
			fail("Unable to copy ServicesMetaData.json test file into testDb folder : " + e.getMessage());
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
        assertEquals(1, result.getLogDetails().size());
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
        assertEquals(1, result.getLogDetails().size());
		patchService.save(p);
		patchService.log(p.getPatchNumber(),pld2);
		patchService.save(p);
		patchService.log(p.getPatchNumber(),pld3);
		result = patchService.findPatchLogById(patchNumber);
        assertEquals(3, result.getLogDetails().size());
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
		DBPatch dbPatch = DBPatch.builder().dbPatchBranch("patchBRanch").build();
		dbPatch.addDbObject(DbObject.builder()
				.fileName("FileName1")
				.filePath("FilePath1").build());
		dbPatch.addDbObject(DbObject.builder()
				.fileName("FileName2")
				.filePath("FilePath2").build());
		Patch patch = Patch.builder().patchNumber("SomeUnqiueNumber3")
				.dbPatch(dbPatch)
				.services(Lists.newArrayList(service))
				.build();
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

		DBPatch dbPatch = DBPatch.builder().build();
		dbPatch.addDbObject(DbObject.builder()
				.fileName("test-db1")
				.filePath("com.apgsga.ch/sql/db/test-db1")
				.moduleName("test-db1")
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
				.dbPatch(dbPatch)
				.build();

		DBPatch dbPatch2 = DBPatch.builder().build();
		dbPatch2.addDbObject(DbObject.builder()
				.fileName("test-db2")
				.filePath("com.apgsga.ch/sql/db/test-db2")
				.moduleName("test-db2")
				.build());

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
				.dbPatch(dbPatch2)
				.build();
		patchService.save(p1Updated);
		patchService.save(p2Updated);
        assertEquals(2, patchService.findById("p1").retrieveAllArtifactsToPatch().size());
        assertEquals(2, patchService.findById("p2").retrieveAllArtifactsToPatch().size());
        assertEquals(1, patchService.findWithObjectName("ma1").size());
        assertEquals(1, patchService.findWithObjectName("ma2").size());
        assertEquals(2, patchService.findWithObjectName("ma3").size());
        assertEquals(0, patchService.findWithObjectName("wrongName").size());
        assertEquals(1, patchService.findWithObjectName("test-db2").size());
	}

	@Test
	public void testAssembleAndDeployStartPipelineForNonExistingPatch() {
		Patch p = Patch.builder().patchNumber("5401").build();
		Patch p2 = Patch.builder().patchNumber("5402").build();
		patchService.save(p);
		patchService.save(p2);

		LinkedHashSet<String> patchNumbers = Sets.newLinkedHashSet();
		patchNumbers.add("5401");
		patchNumbers.add("5402");

		AssembleAndDeployParameters params = AssembleAndDeployParameters.builder()
											.target("DEV-JHE")
											.errorNotification("error")
											.successNotification("success")
											.patchNumbers(patchNumbers)
											.build();
		patchService.startAssembleAndDeployPipeline(params);
	}

	@Test
	public void testAssembleAndDeployStartPipelineForExistingPatch() {
		Patch p = Patch.builder().patchNumber("5401").build();
		Patch p2 = Patch.builder().patchNumber("5402").build();
		patchService.save(p);
		patchService.save(p2);

		LinkedHashSet<String> patchNumbers = Sets.newLinkedHashSet();
		patchNumbers.add("5401");
		patchNumbers.add("5402");

		AssembleAndDeployParameters params = AssembleAndDeployParameters.builder()
					.target("DEV-JHE")
					.errorNotification("error")
					.successNotification("success")
					.patchNumbers(patchNumbers)
					.build();
		patchService.startAssembleAndDeployPipeline(params);
	}

	@Test
	public void testStartInstallPipeline() {

		LinkedHashSet patchNumbers = Sets.newLinkedHashSet();
		patchNumbers.add("5401");

		InstallParameters params = InstallParameters.builder()
				.target("dev-jhe")
				.patchNumbers(patchNumbers)
				.successNotification("success")
				.errorNotification("error")
				.build();
		patchService.startInstallPipeline(params);
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
	public void testPatchServiceSetup() {
		Service service = Service.builder()
				.serviceName("SomeOtherService")
				.build();
		List<Service> services = Lists.newArrayList(service);
		Patch p = Patch.builder()
				.patchNumber("8000")
				.services(services)
				.build();
		patchService.save(p);
		SetupParameter sp = SetupParameter.builder()
				.patchNumber("8000")
				.successNotification("success")
				.errorNotification("error")
				.build();
		patchService.setup(sp);
		Patch updatedPatch = patchService.findById("8000");
		Assert.assertTrue("ServiceMetadata has not been added", updatedPatch.getServices().get(0).getServiceMetaData() != null);
	}

	@Test
	public void testOnDemandPipeline() {
		Patch p = Patch.builder().patchNumber("5401").build();
		patchService.save(p);
		OnDemandParameter params = OnDemandParameter.builder()
				.target("DEV-JHE")
				.patchNumber("5401")
				.build();
		patchService.startOnDemandPipeline(params);
	}

	@Test
	public void testOnClonePipeline() {
		Patch p = Patch.builder().patchNumber("5401").build();
		Patch p2 = Patch.builder().patchNumber("5402").build();
		patchService.save(p);
		patchService.save(p2);
		OnCloneParameters onCloneParameters = OnCloneParameters.builder()
				.src("TEST-SRC")
				.target("TEST_TARGET")
				.patchNumbers(Sets.newHashSet("5401","5402"))
				.build();
		patchService.startOnClonePipeline(onCloneParameters);
	}

	@Test
	// JHE : Test mainly done in order to test the syntax with Streams expression
	public void testRetrieveASpecificTargetInstance() {

		String searchedTarget = "DEV-CHEI211";
		String searchedService = "digiflex";

		TargetInstance targetInstance = patchService.getRepo().targetInstances().getTargetInstances().stream().filter(ti -> ti.getName().equals(searchedTarget)).findFirst().get();

		Assert.assertNotNull(targetInstance);
		Assert.assertEquals(targetInstance.getName(),searchedTarget);
		Assert.assertEquals("dev-digiflex-e.apgsga.ch",targetInstance.getServices().stream().filter(s -> s.getServiceName().equals(searchedService)).findFirst().get().getInstallationHost());
	}
}
