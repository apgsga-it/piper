package com.apgsga.microservice.patch.server.impl.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

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

import com.apgsga.microservice.patch.api.DbModules;
import com.apgsga.microservice.patch.api.DbObject;
import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.api.ServiceMetaData;
import com.apgsga.microservice.patch.api.ServicesMetaData;
import com.apgsga.microservice.patch.api.impl.DbObjectBean;
import com.apgsga.microservice.patch.api.impl.MavenArtifactBean;
import com.apgsga.microservice.patch.api.impl.ServiceMetaDataBean;
import com.apgsga.microservice.patch.api.impl.ServicesMetaDataBean;

@RunWith(SpringRunner.class)
@TestPropertySource(properties = { "dblocation=db", "dbworkdir=work" })
public class FilebasedPersistenceTest {

	private PatchPersistence repo;

	@Value("${dblocation}")
	private String dbLocation;

	@Value("${dbworkdir}")
	private String dbWorkLocation;

	@Before
	public void setUp() {
		// It self a test ;-)
		final ResourceLoader rl = new FileSystemResourceLoader();
		Resource db = rl.getResource(dbLocation);
		Resource workDir = rl.getResource(dbWorkLocation);
		repo = new FilebasedPatchPersistence(db, workDir);
		Resource testResources = rl.getResource("src/test/resources/json");
		final PatchPersistence per = new FilebasedPatchPersistence(testResources, workDir);
		Patch testPatch5401 = per.findById("5401");
		Patch testPatch5402 = per.findById("5402");
		repo.clean();

		try {
			File persistSt = new File(dbLocation);
			FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/ServicesMetaData.json"),
					new File(persistSt, "ServicesMetaData.json"));
		} catch (IOException e) {
			Assert.fail("Unable to copy ServiceData.json test file into testDb folder");
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
		result.setBaseVersionNumber("XXXX");
		List<DbObject> dbOList = Lists.newArrayList();
		dbOList.add(new DbObjectBean("FileName1", "FilePath1"));
		result.setDbObjects(dbOList);
		repo.savePatch(result);
		Patch upDatedresult = repo.findById("5402");
		assertNotNull(upDatedresult);
		assertEquals("XXXX", upDatedresult.getBaseVersionNumber());
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
		assertTrue(dbModulesRead.size() == 2);
		dbModulesRead.forEach(m -> {
			assertTrue(m.equals("testdbmodule") || m.equals("testdbAnotherdbModule"));
		});
	}

	@Test
	public void testServicesMetaData() {
		List<ServiceMetaData> serviceList = Lists.newArrayList();
		MavenArtifactBean it21UiStarter = new MavenArtifactBean();
		it21UiStarter.setArtifactId("it21ui-app-starter");
		it21UiStarter.setGroupId("com.apgsga.it21.ui.mdt");
		it21UiStarter.setName("it21ui-app-starter");

		MavenArtifactBean jadasStarter = new MavenArtifactBean();
		jadasStarter.setArtifactId("jadas-app-starter");
		jadasStarter.setGroupId("com.apgsga.it21.ui.mdt");
		jadasStarter.setName("jadas-app-starter");

		final ServiceMetaData it21Ui = new ServiceMetaDataBean("It21Ui", "it21_release_9_0_6_admin_uimig", "9.1.0",
				"ADMIN-UIMIG");
		serviceList.add(it21Ui);
		final ServiceMetaData someOtherService = new ServiceMetaDataBean("SomeOtherService",
				"it21_release_9_0_6_some_tag", "9.1.0", "SOME-TAG");
		serviceList.add(someOtherService);
		final ServicesMetaData data = new ServicesMetaDataBean();
		data.setServicesMetaData(serviceList);
		repo.saveServicesMetaData(data);
		ServicesMetaData serviceData = repo.getServicesMetaData();
		assertEquals(data, serviceData);
	}

}
