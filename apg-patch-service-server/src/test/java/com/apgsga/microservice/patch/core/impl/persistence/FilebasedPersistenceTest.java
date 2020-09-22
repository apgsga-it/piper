package com.apgsga.microservice.patch.core.impl.persistence;

import com.apgsga.microservice.patch.api.*;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@TestPropertySource(properties = { "dblocation=db", "dbworkdir=work" })
public class FilebasedPersistenceTest {

	private PatchPersistence repo;

	private PatchSystemMetaInfoPersistence patchSystemInfoRepo;

	@Value("${dblocation}")
	private String dbLocation;

	@Value("${dbworkdir}")
	private String dbWorkLocation;

	@Before
	public void setUp() throws IOException {
		// It self a test ;-)
		final ResourceLoader rl = new FileSystemResourceLoader();
		Resource db = rl.getResource(dbLocation);
		Resource workDir = rl.getResource(dbWorkLocation);
		repo = new FilebasedPatchPersistence(db, workDir);
		patchSystemInfoRepo = new FilePatchSystemMetaInfoPersistence(db,workDir);
		Resource testResources = rl.getResource("src/test/resources/json");
		final PatchPersistence per = new FilebasedPatchPersistence(testResources, workDir);
		Patch testPatch5401 = per.findById("5401");
		Patch testPatch5402 = per.findById("5402");
		repo.clean();

		try {
			File persistSt = new File(dbLocation);
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/ServicesMetaData.json"), new File(persistSt, "ServicesMetaData.json"));
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/OnDemandTargets.json"), new File(persistSt, "OnDemandTargets.json"));
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/StageMappings.json"), new File(persistSt, "StageMappings.json"));
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/TargetInstances.json"), new File(persistSt, "TargetInstances.json"));
		} catch (IOException e) {
			Assert.fail("Unable to copy JSON test files into testDb folder");
		}

		repo.savePatch(testPatch5401);
		repo.savePatch(testPatch5402);
	}

	@Test
	public void testFindById() {
		Patch result = repo.findById("5401");
		assertNotNull(result);
		assertEquals("5401", result.getPatchNummer());
	}

	@Test
	public void testExistsTrue() {
		assertEquals(true, repo.patchExists("5401"));
	}

	@Test
	public void testExistsFalse() {
		assertEquals(false, repo.patchExists("xxxxx"));
	}

	@Test
	public void testUpdate() {
		Patch result = repo.findById("5402");
		assertNotNull(result);
		Service service = Service.create().serviceName("It21Ui").baseVersionNumber("XXXX");
		result.addServices(service);
		List<DbObject> dbOList = Lists.newArrayList();
		dbOList.add(new DbObject("FileName1", "FilePath1"));
		result.setDbObjects(dbOList);
		repo.savePatch(result);
		Patch upDatedresult = repo.findById("5402");
		assertNotNull(upDatedresult);
		service = upDatedresult.getService("It21Ui");
		assertEquals("XXXX", service.getBaseVersionNumber());
		List<DbObject> dbObjects = upDatedresult.getDbObjects();
		assertEquals(1, dbObjects.size());
		DbObject dbObject = dbObjects.get(0);
		assertEquals("FileName1", dbObject.getFileName());
		assertEquals("FilePath1", dbObject.getFilePath());

	}

	@Test
	public void testFindServiceByName() {
		assertNotNull(repo.findServiceByName("It21Ui"));
		assertNotNull(repo.findServiceByName("SomeOtherService"));
	}

	@Test
	public void testSaveModules() {
		List<String> dbModulesList = Lists.newArrayList("testdbmodule", "testdbAnotherdbModule");
		DbModules intialLoad = new DbModules(dbModulesList);
		repo.saveDbModules(intialLoad);
		DbModules dbModules = repo.getDbModules();
		List<String> dbModulesRead = dbModules.getDbModules();
		assertEquals(2, dbModulesRead.size());
		for (String m : dbModulesRead) {
			assertTrue(m.equals("testdbmodule") || m.equals("testdbAnotherdbModule"));
		}
	}

	@Test
	public void testServicesMetaData() {
		List<ServiceMetaData> serviceList = Lists.newArrayList();
		MavenArtifact it21UiStarter = new MavenArtifact();
		it21UiStarter.setArtifactId("it21ui-app-starter");
		it21UiStarter.setGroupId("com.apgsga.it21.ui.mdt");
		it21UiStarter.setName("it21ui-app-starter");

		MavenArtifact jadasStarter = new MavenArtifact();
		jadasStarter.setArtifactId("jadas-app-starter");
		jadasStarter.setGroupId("com.apgsga.it21.ui.mdt");
		jadasStarter.setName("jadas-app-starter");

		final ServiceMetaData it21Ui = new ServiceMetaData("It21Ui", "it21_release_9_1_0_admin_uimig", "9.1.0",
				"ADMIN-UIMIG");
		serviceList.add(it21Ui);
		final ServiceMetaData someOtherService = new ServiceMetaData("SomeOtherService",
				"it21_release_9_1_0_some_tag", "9.1.0", "SOME-TAG");
		serviceList.add(someOtherService);
		final ServicesMetaData data = new ServicesMetaData();
		data.setServicesMetaData(serviceList);
		repo.saveServicesMetaData(data);
		ServicesMetaData serviceData = repo.getServicesMetaData();
		assertEquals(data, serviceData);
	}

	@Test
	public void testLoadOnDemandTargets() {
		OnDemandTargets onDemandTargets = patchSystemInfoRepo.onDemandTargets();
		assertEquals(4,onDemandTargets.getOnDemandTargets().size());
		assertTrue(onDemandTargets.getOnDemandTargets().contains("DEV-CHEI212"));
		assertTrue(onDemandTargets.getOnDemandTargets().contains("DEV-CHEI211"));
		assertTrue(onDemandTargets.getOnDemandTargets().contains("DEV-CM"));
		assertTrue(onDemandTargets.getOnDemandTargets().contains("DEV-JHE"));
	}

	@Test
	public void testLoadStageMappings() {
		StageMappings stageMappings = patchSystemInfoRepo.stageMappings();
		assertEquals(4,stageMappings.getStageMappings().size());
		for(StageMapping stageMapping : stageMappings.getStageMappings()) {
			// JHE : Just test two, not even sure it makes sense to load a complete file
			if(stageMapping.equals("Entwicklung")) {
				assertEquals("DEV-CHEI212",stageMapping.getTarget());
			}
			if(stageMapping.equals("Produktion")) {
				assertEquals("DEV-CHPI211",stageMapping.getTarget());
			}
			assertEquals(2,stageMapping.getStages().size());
		}
	}

	@Test
	public void testLoadTargetInstances() {
		TargetInstances targetInstances = patchSystemInfoRepo.targetInstances();
		assertEquals(4,targetInstances.getTargetInstances().size());
		for(TargetInstance targetInstance : targetInstances.getTargetInstances()) {
			// JHE : Just test two, not even sure it makes sense to load a complete file
			if(targetInstance.getName().equals("DEV-CHPI211")) {
				assertEquals(5,targetInstance.getServices().size());
				List<String> devChpi211ServiceNames = Stream.of("it21-db","ds-db","digiflex","jadas","it21_ui").collect(Collectors.toList());
				for(ServiceMetaData serviceMetaData : targetInstance.getServices()) {
					assertTrue(devChpi211ServiceNames.contains(serviceMetaData.getServiceName()));
				}
			}
			if(targetInstance.getName().equals("DEV-CHQI211")) {
				assertEquals(2,targetInstance.getServices().size());
				List<String> devChqi211ServiceNames = Stream.of("it21-db","it21_ui").collect(Collectors.toList());
				List<String> devChqi211NoServiceNames = Stream.of("ds-db","digiflex","jadas").collect(Collectors.toList());
				for(ServiceMetaData serviceMetaData : targetInstance.getServices()) {
					assertTrue(devChqi211ServiceNames.contains(serviceMetaData.getServiceName()));
					assertFalse(devChqi211NoServiceNames.contains(serviceMetaData.getServiceName()));
				}
			}
		}
	}

	@Test
	public void testStageMappingFor() {
		StageMapping stageMapping = patchSystemInfoRepo.stageMappingFor("EntwicklungInstallationsbereit");
		assertEquals("Entwicklung",stageMapping.getName());
		assertEquals("DEV-CHEI212",stageMapping.getTarget());
		for(Stage stage : stageMapping.getStages()) {
			Stream.of("startPipelineAndTag","cancel").collect(Collectors.toList()).contains(stage.getName());
			Stream.of("2","0").collect(Collectors.toList()).contains(stage.getCode());
			Stream.of("com.apgsga.microservice.patch.server.impl.EntwicklungInstallationsbereitAction","com.apgsga.microservice.patch.server.impl.PipelineInputAction").collect(Collectors.toList()).contains(stage.getImplcls());
			Stream.of("Installationsbereit","").collect(Collectors.toList()).contains(stage.getToState());
		}
	}
}