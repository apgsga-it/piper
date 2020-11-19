package com.apgsga.microservice.patch.core;

import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.core.impl.SimplePatchContainerBean;
import com.apgsga.microservice.patch.exceptions.PatchServiceRuntimeException;
import com.apgsga.microservice.patch.server.MicroPatchServer;
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

import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@TestPropertySource(locations = "application-test.properties")
@SpringBootTest(classes = MicroPatchServer.class)
@ActiveProfiles("test,mock,mockMavenRepo,groovyactions")
public class MicroServicePatchServerExceptionTests {

	protected final Log LOGGER = LogFactory.getLog(getClass());

	@Value("${json.meta.info.db.location}")
	private String metaInfoDb;

	@Autowired
	private SimplePatchContainerBean patchService;

	@Autowired
	@Qualifier("patchPersistence")
	private PatchPersistence repo;

	@Before
	public void setUp() {
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
	public void testFindByNullIdException() {
		try {
			patchService.findById(null);
			patchService.findById("");
			fail();
		} catch (PatchServiceRuntimeException e) {
			LOGGER.info(e.toString());
			Assert.assertEquals("FilebasedPatchPersistence.findById.patchnumber.notnullorempty.assert", e.getMessageKey());
		}
	}
	
	@Test
	public void testFindPatchLogByNullIdException() {
		try {
			patchService.findPatchLogById(null);
			patchService.findPatchLogById("");
			fail();
		} catch(PatchServiceRuntimeException e) {
			LOGGER.info(e.toString());
			Assert.assertEquals("FilebasedPatchPersistence.findById.patchlognumber.notnullorempty.assert", e.getMessageKey());
		}
	}
	
	@Test
	public void testSavePatchNullException() {
		try {
			patchService.save(null);
			fail();
		} catch (PatchServiceRuntimeException e) {
			LOGGER.info(e.toString());
			Assert.assertEquals("SimplePatchContainerBean.save.patchobject.notnull.assert", e.getMessageKey());
		}
	}
	
	@Test
	public void testSavePatchLogNullException() {
		try {
			patchService.log(null,null);
			fail();
		} catch(PatchServiceRuntimeException e) {
			LOGGER.info(e.toString());
			Assert.assertEquals("SimplePatchContainerBean.log.patchnumber.isnullorempty", e.getMessageKey());
		}
	}
	
	@Test
	public void testSaveEmptyWithOutId() {
		Patch patch = new Patch();
		try {
			patchService.save(patch);
			fail();
		} catch (PatchServiceRuntimeException e) {
			LOGGER.info(e.toString());
			Assert.assertEquals("SimplePatchContainerBean.save.patchnumber.notnullorempty.assert", e.getMessageKey());
		}
	}
	
	@Test
	public void testRemovePatchNullException() {
		try {
			patchService.remove(null);
			fail();
		} catch (PatchServiceRuntimeException e) {
			LOGGER.info(e.toString());
			Assert.assertEquals("FilebasedPatchPersistence.remove.patchobject.notnull.assert", e.getMessageKey());
		}
	}
	
	@Test
	public void testRemoveEmptyWithOutId() {
		Patch patch = new Patch();
		try {
			patchService.remove(patch);
			fail();
		} catch (PatchServiceRuntimeException e) {
			LOGGER.info(e.toString());
			Assert.assertEquals("FilebasedPatchPersistence.remove.patchnumber.notnullorempty.assert", e.getMessageKey());
		}
	}
	
	@Test
	public void testRemoveDoesnotExist() {
		Patch patch = new Patch();
		patch.setPatchNummer("XXXX");
		try {
			patchService.remove(patch);
			fail();
		} catch (PatchServiceRuntimeException e) {
			LOGGER.info(e.toString());
			Assert.assertEquals("FilebasedPatchPersistence.remove.patch.exists.assert", e.getMessageKey());
		}
	}
	
	@Test
	public void testListModulesWherePatchDoesntExist() {
		try {
			patchService.listAllObjectsChangedForDbModule("XXXXX", "SomeSearchString"); 
			fail("Expected Runtime Exception");
		} catch (PatchServiceRuntimeException e) {
			LOGGER.info(e.toString());
			Assert.assertEquals("SimplePatchContainerBean.listAllObjectsChangedForDbModule.patch.exists.assert", e.getMessageKey());
		}
	}

	@Test
	public void testBuildPatchForInvalidPatchNumber() {
		try {
			patchService.build("xxxx","dev-chei212,","dev-informatiktest");
			fail("A runtime exception was expected");
		}
		catch (PatchServiceRuntimeException e) {
			LOGGER.info(e.toString());
			Assert.assertEquals("SimplePatchContainerBean.build.patch.exists.assert",e.getMessageKey());
		}
	}

	@Test
	public void testBuildPatchForNullTarget() {
		try {
			Patch p = new Patch();
			p.setPatchNummer("2222");
			patchService.save(p);
			Assert.assertTrue("Patch 2222 hasn't been saved correctly",patchService.findById("2222") != null);
			patchService.build("2222",null, "dev-informatiktest");
			fail("A runtime exception was expected");
		}
		catch (PatchServiceRuntimeException e) {
			LOGGER.info(e.toString());
			Assert.assertEquals("SimplePatchContainerBean.build.target.notnull",e.getMessageKey());
		}
	}
}
