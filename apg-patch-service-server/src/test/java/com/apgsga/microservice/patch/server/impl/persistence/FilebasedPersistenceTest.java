package com.apgsga.microservice.patch.server.impl.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileCopyUtils;

import com.apgsga.microservice.patch.api.DbObject;
import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchOpService;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.api.impl.DbObjectBean;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "test.properties")
@ContextConfiguration(classes = { FilebasedPersistenceTest.TestConfiguration.class })
public class FilebasedPersistenceTest {

	@Autowired
	@Qualifier("patchPersistence")
	private PatchPersistence repo;
	
	@Value("${json.db.location:db}")
	private String dbLocation;
	

	@Before
	public void setUp() {
		// It self a test ;-)
		final ResourceLoader rl = new FileSystemResourceLoader();
		Resource testResources = rl.getResource("src/test/resources/json");
		final PatchPersistence per = new FilebasedPatchPersistence(testResources);
		Patch testPatch5401 = per.findById("5401");
		Patch testPatch5402 = per.findById("5402");
		repo.clean();
		
		try {
			File persistSt = new File(dbLocation); 
			FileCopyUtils.copy(new File(testResources.getURI().getPath()+"/ServicesMetaData.json"), new File(persistSt,"ServicesMetaData.json"));
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
		ResourceLoader rl = new FileSystemResourceLoader();
		Resource storagePath = rl.getResource(dbLocation);
		FilebasedPatchPersistence persistence = new FilebasedPatchPersistence(storagePath);
		Patch result = persistence.findById("5402");
		assertNotNull(result);
		result.setBaseVersionNumber("XXXX");
		List<DbObject> dbOList = Lists.newArrayList();
		dbOList.add(new DbObjectBean("FileName1", "FilePath1"));
		result.setDbObjects(dbOList);
		persistence.savePatch(result);
		Patch upDatedresult = persistence.findById("5402");
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

	@Configurable
	static class TestConfiguration {
		@Value("${json.db.location:db}")
		private String dbLocation;

		@Bean(name = "patchPersistence")
		public PatchPersistence patchFilebasePersistence() throws IOException {
			final ResourceLoader rl = new FileSystemResourceLoader();
			Resource dbStorabe = rl.getResource(dbLocation);
			final PatchPersistence per = new FilebasedPatchPersistence(dbStorabe);
			per.init();
			return per;
		}
	}

}
