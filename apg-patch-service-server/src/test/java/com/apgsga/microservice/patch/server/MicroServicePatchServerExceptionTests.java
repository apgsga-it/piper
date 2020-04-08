package com.apgsga.microservice.patch.server;

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

import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.api.impl.MavenArtifactBean;
import com.apgsga.microservice.patch.exceptions.PatchServiceRuntimeException;
import com.apgsga.microservice.patch.server.impl.GroovyScriptActionExecutor;
import com.apgsga.microservice.patch.server.impl.PatchActionExecutor;
import com.apgsga.microservice.patch.server.impl.PatchActionExecutorFactory;
import com.apgsga.microservice.patch.server.impl.SimplePatchContainerBean;

@RunWith(SpringRunner.class)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@TestPropertySource(locations = "application-test.properties")
@SpringBootTest(classes = MicroPatchServer.class)
@ActiveProfiles("test,mock,mockMavenRepo,groovyactions")
public class MicroServicePatchServerExceptionTests {

	protected final Log LOGGER = LogFactory.getLog(getClass());

	@Autowired
	private SimplePatchContainerBean patchService;

	@Autowired
	@Qualifier("patchPersistence")
	private PatchPersistence repo;

	@Autowired
	private PatchActionExecutorFactory patchActionFactory;

	@Before
	public void setUp() {
		repo.clean();
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
			patchService.log(null);
			fail();
		} catch(PatchServiceRuntimeException e) {
			LOGGER.info(e.toString());
			Assert.assertEquals("SimplePatchContainerBean.log.patch.null.assert", e.getMessageKey());
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
	public void testPatchInvalidToState() {
		Patch patch = new Patch();
		patch.setPatchNummer("SomeUnqiueNumber3");
		patch.setServiceName("It21ui");
		patch.setMicroServiceBranch("SomeBaseBranch");
		patch.addMavenArtifacts(new MavenArtifactBean("ArtifactId1", "GroupId1", "Version1"));
		patch.addMavenArtifacts(new MavenArtifactBean("ArtifactId2", "GroupId2", "Version2"));
		patchService.save(patch);
		PatchActionExecutor patchActionExecutor = patchActionFactory.create(patchService); 
		try {
			patchActionExecutor.execute("SomeUnqiueNumber3", "xxxxxxx");
			fail("Expected Runtime Exception");
		} catch (PatchServiceRuntimeException e) {
			LOGGER.info(e.toString());
			Assert.assertEquals("Groovy.script.executePatchAction.state.exits.assert", e.getMessageKey());
		}
	}
	
	@Test
	public void testPatchConfigDirDoesnotExist() {
		Patch patch = new Patch();
		patch.setPatchNummer("SomeUnqiueNumber3");
		patch.setServiceName("It21ui");
		patch.setMicroServiceBranch("SomeBaseBranch");
		patch.addMavenArtifacts(new MavenArtifactBean("ArtifactId1", "GroupId1", "Version1"));
		patch.addMavenArtifacts(new MavenArtifactBean("ArtifactId2", "GroupId2", "Version2"));
		patchService.save(patch);
		GroovyScriptActionExecutor patchActionExecutor = (GroovyScriptActionExecutor) patchActionFactory.create(patchService); 
		patchActionExecutor.setConfigDir("XXXXXXXX");
		try {
			patchActionExecutor.execute("SomeUnqiueNumber3", "xxxxxxx");
			fail("Expected Runtime Exception");
		} catch (PatchServiceRuntimeException e) {
			LOGGER.info(e.toString());
			Assert.assertEquals("Groovy.script.executePatchAction.configdir.exists.assert", e.getMessageKey());
		}
	}
	
	@Test
	public void testPatchConfigFileDoesnotExist() {
		Patch patch = new Patch();
		patch.setPatchNummer("SomeUnqiueNumber3");
		patch.setServiceName("It21ui");
		patch.setMicroServiceBranch("SomeBaseBranch");
		patch.addMavenArtifacts(new MavenArtifactBean("ArtifactId1", "GroupId1", "Version1"));
		patch.addMavenArtifacts(new MavenArtifactBean("ArtifactId2", "GroupId2", "Version2"));
		patchService.save(patch);
		GroovyScriptActionExecutor patchActionExecutor = (GroovyScriptActionExecutor) patchActionFactory.create(patchService); 
		patchActionExecutor.setConfigFileName("XXXXXX");
		try {
			patchActionExecutor.execute("SomeUnqiueNumber3", "xxxxxxx");
			fail("Expected Runtime Exception");
		} catch (PatchServiceRuntimeException e) {
			LOGGER.info(e.toString());
			Assert.assertEquals("Groovy.script.executePatchAction.configfile.exists.assert", e.getMessageKey());
		}
	}
	
	
	@Test
	public void testPatchInvalidGroovyScriptFile() {
		Patch patch = new Patch();
		patch.setPatchNummer("SomeUnqiueNumber3");
		patch.setServiceName("It21ui");
		patch.setMicroServiceBranch("SomeBaseBranch");
		patch.addMavenArtifacts(new MavenArtifactBean("ArtifactId1", "GroupId1", "Version1"));
		patch.addMavenArtifacts(new MavenArtifactBean("ArtifactId2", "GroupId2", "Version2"));
		patchService.save(patch);
		GroovyScriptActionExecutor patchActionExecutor = (GroovyScriptActionExecutor) patchActionFactory.create(patchService); 
		patchActionExecutor.setGroovyScriptFile("XXXXX");
		try {
			patchActionExecutor.execute("SomeUnqiueNumber3", "xxxxxxx");
			fail("Expected Runtime Exception");
		} catch (PatchServiceRuntimeException e) {
			LOGGER.info(e.toString());
			Assert.assertEquals("GroovyScriptActionExecutor.execute.scriptfileexists.assert", e.getMessageKey());
		}
	}
	
	
	
	@Test
	public void testPatchExeuteToStatePatchNumberNullOrEmpy() {
		GroovyScriptActionExecutor patchActionExecutor = (GroovyScriptActionExecutor) patchActionFactory.create(patchService); 
		patchActionExecutor.setGroovyScriptFile("XXXXX");
		try {
			patchActionExecutor.execute("", "xxxxxxx");
			fail("Expected Runtime Exception");
		} catch (PatchServiceRuntimeException e) {
			LOGGER.info(e.toString());
			Assert.assertEquals("GroovyScriptActionExecutor.execute.patchnumber.notnullorempty.assert", e.getMessageKey());
		}
	}
	
	@Test
	public void testPatchExeuteToStatePatchNumberDoesnotExist() {
		GroovyScriptActionExecutor patchActionExecutor = (GroovyScriptActionExecutor) patchActionFactory.create(patchService); 
		patchActionExecutor.setGroovyScriptFile("XXXXX");
		try {
			patchActionExecutor.execute("xxxxxx", "xxxxxxx");
			fail("Expected Runtime Exception");
		} catch (PatchServiceRuntimeException e) {
			LOGGER.info(e.toString());
			Assert.assertEquals("GroovyScriptActionExecutor.execute.patch.exists.assert", e.getMessageKey());
		}
	}

}
