package com.apgsga.microservice.patch.core.impl.persistence;

import com.apgsga.microservice.patch.api.Package;
import com.apgsga.microservice.patch.api.*;
import com.apgsga.patch.db.integration.impl.PatchRdbmsMockImpl;
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
public class PatchPersistenceTest {

	private PatchPersistence repo;

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
		repo = new PatchPersistenceImpl(db, workDir ,new PatchRdbmsMockImpl());
		Resource testResources = rl.getResource("src/test/resources/json");
		final PatchPersistence per = new PatchPersistenceImpl(testResources, workDir, new PatchRdbmsMockImpl());
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
		assertEquals("5401", result.getPatchNumber());
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
		Patch patchToSave = result.toBuilder()
				.services(Lists.newArrayList(Service.builder().serviceName("It21Ui").build()))
				.dbObjects(Lists.newArrayList(DbObject.builder().fileName("FileName1").filePath("FilePath1").build())).build();
		repo.savePatch(patchToSave);
		Patch updated = repo.findById("5402");
		assertNotNull(updated);
		Service service = updated.getService("It21Ui");
		assertNotNull(service);
		List<DbObject> dbObjects = updated.getDbObjects();
		assertEquals(1, dbObjects.size());
		DbObject dbObject = dbObjects.get(0);
		assertEquals("FileName1", dbObject.getFileName());
		assertEquals("FilePath1", dbObject.getFilePath());

	}

	@Test
	public void testFindServiceByName() {
		assertNotNull(repo.getServiceMetaDataByName("It21Ui"));
		assertNotNull(repo.getServiceMetaDataByName("SomeOtherService"));
	}

	@Test
	public void testSaveModules() {
		List<String> dbModulesList = Lists.newArrayList("testdbmodule", "testdbAnotherdbModule");
		DbModules intialLoad = DbModules.builder().dbModules(dbModulesList).build();
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
		final MavenArtifact bomData = MavenArtifact.builder()
				.artifactId("bomArtifactid")
				.groupId("bomGroupid")
				.name("whatevername")
				.build();
		final Package pkgData = Package.builder()
				.packagerName("jadasPackager")
				.starterCoordinates(com.google.common.collect.Lists.newArrayList("com.apgsga.it21.ui.mdt:it21ui-app-starter", "com.apgsga.it21.ui.mdt:jadas-app-starter"))
				.build();
		final ServiceMetaData serviciceMetaData = ServiceMetaData.builder()
				.bomCoordinates(bomData)
				.baseVersionNumber("aBaseVersionNumber")
				.microServiceBranch("it21_release_9_1_0_admin_uimig")
				.packages(com.google.common.collect.Lists.newArrayList(pkgData))
				.revisionMnemoPart("ADMIN-UIMIG")
				.serviceName("jadasserver").build();
		final ServicesMetaData servicesMetaData = ServicesMetaData.builder().servicesMetaData(com.google.common.collect.Lists.newArrayList(serviciceMetaData)).build();
		repo.saveServicesMetaData(servicesMetaData);
		ServicesMetaData serviceData = repo.getServicesMetaData();
		assertEquals(servicesMetaData, serviceData);
	}

	@Test
	public void testLoadOnDemandTargets() {
		OnDemandTargets onDemandTargets = repo.onDemandTargets();
		assertEquals(4,onDemandTargets.getOnDemandTargets().size());
		assertTrue(onDemandTargets.getOnDemandTargets().contains("DEV-CHEI212"));
		assertTrue(onDemandTargets.getOnDemandTargets().contains("DEV-CHEI211"));
		assertTrue(onDemandTargets.getOnDemandTargets().contains("DEV-CM"));
		assertTrue(onDemandTargets.getOnDemandTargets().contains("DEV-JHE"));
	}

	@Test
	public void testLoadStageMappings() {
		StageMappings stageMappings = repo.stageMappings();
		assertEquals(4,stageMappings.getStageMappings().size());
		for(StageMapping stageMapping : stageMappings.getStageMappings()) {
			// JHE : Just test two, not even sure it makes sense to load a complete file
			if(stageMapping.getName().equals("Entwicklung")) {
				assertEquals("DEV-CHEI212",stageMapping.getTarget());
			}
			if(stageMapping.getName().equals("Produktion")) {
				assertEquals("DEV-CHPI211",stageMapping.getTarget());
			}
		}
	}

	@Test
	public void testLoadTargetInstances() {
		TargetInstances targetInstances = repo.targetInstances();
		assertEquals(4,targetInstances.getTargetInstances().size());
		for(TargetInstance targetInstance : targetInstances.getTargetInstances()) {
			// JHE : Just test two, not even sure it makes sense to load a complete file
			if(targetInstance.getName().equals("DEV-CHPI211")) {
				assertEquals(5,targetInstance.getServices().size());
				List<String> devChpi211ServiceNames = Stream.of("it21-db","ds-db","digiflex","jadas","it21_ui").collect(Collectors.toList());
				for(ServiceInstallation serviceMetaData : targetInstance.getServices()) {
					assertTrue(devChpi211ServiceNames.contains(serviceMetaData.getServiceName()));
				}
			}
			if(targetInstance.getName().equals("DEV-CHQI211")) {
				assertEquals(2,targetInstance.getServices().size());
				List<String> devChqi211ServiceNames = Stream.of("it21-db","it21_ui").collect(Collectors.toList());
				List<String> devChqi211NoServiceNames = Stream.of("ds-db","digiflex","jadas").collect(Collectors.toList());
				for(ServiceInstallation serviceMetaData : targetInstance.getServices()) {
					assertTrue(devChqi211ServiceNames.contains(serviceMetaData.getServiceName()));
					assertFalse(devChqi211NoServiceNames.contains(serviceMetaData.getServiceName()));
				}
			}
		}
	}

	@Test
	public void testTargetFor() {
		String target = repo.targetFor("Informatiktest");
		assertEquals("DEV-CHEI211", target);
	}

	@Test
	public void testPackagernameFor() {
		final MavenArtifact bomData = MavenArtifact.builder()
				.artifactId("anotherBomId")
				.groupId("anotherbomGroupid")
				.name("anotherwhatevername")
				.build();
		final Package pkgData = Package.builder()
				.packagerName("packagerForXyzservice")
				.starterCoordinates(com.google.common.collect.Lists.newArrayList("com.apgsga.it21.ui.mdt:it21ui-app-starter", "com.apgsga.it21.ui.mdt:jadas-app-starter"))
				.build();
		final ServiceMetaData serviciceMetaData = ServiceMetaData.builder()
				.bomCoordinates(bomData)
				.baseVersionNumber("aBaseVersionNumber")
				.microServiceBranch("it21_release_9_1_0_admin_uimig")
				.packages(com.google.common.collect.Lists.newArrayList(pkgData))
				.revisionMnemoPart("ADMIN-UIMIG")
				.serviceName("xyzservice").build();
		final ServicesMetaData servicesMetaData = ServicesMetaData.builder().servicesMetaData(com.google.common.collect.Lists.newArrayList(serviciceMetaData)).build();
		repo.saveServicesMetaData(servicesMetaData);
		List<Package> pkgs = repo.packagesFor(Service.builder().serviceName("xyzservice").build());
        assertEquals(1, pkgs.size());
		assertEquals("packagerForXyzservice",pkgs.iterator().next().getPackagerName());
	}
}