package com.apgsga.microservice.patch.client;

import com.apgsga.microservice.patch.api.Package;
import com.apgsga.microservice.patch.api.*;
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
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = {MicroPatchServer.class})
@TestPropertySource(locations = "application-test.properties")
@ActiveProfiles("test,mock,mockMavenRepo,patchOMock,mockDocker")
public class MicroServicePatchClientTest {


    private MicroservicePatchClient patchClient;

    @Autowired
    @Qualifier("patchPersistence")
    private PatchPersistence repo;

    @Value("${json.db.location:target/testdb}")
    private String dbLocation;

    @Value("${json.db.work.location:work}")
    private String dbWorkLocation;

    @Value("${json.meta.info.db.location:target/testdb}")
    private String metaInfoDbLocation;

    @Value("${local.server.port}")
    private String localPort;

    @Before
    public void setUp() {
        patchClient = new MicroservicePatchClient("localhost:" + localPort);
        final ResourceLoader rl = new FileSystemResourceLoader();
        Resource testResources = rl.getResource("src/test/resources/json");
        Resource patchStorage = rl.getResource(dbLocation);
        Resource metaStorage = rl.getResource(metaInfoDbLocation);
        repo.clean();
        try {
            File persistSt = metaStorage.getFile();
            FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/ServicesMetaData.json"), new File(persistSt, "ServicesMetaData.json"));
            FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/OnDemandTargets.json"), new File(persistSt, "OnDemandTargets.json"));
            FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/StageMappings.json"), new File(persistSt, "StageMappings.json"));
            FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/TargetInstances.json"), new File(persistSt, "TargetInstances.json"));
            FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/DbModules.json"), new File(persistSt, "DbModules.json"));
            persistSt = patchStorage.getFile();
            FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/Patch5401.json"), new File(persistSt, "Patch5401.json"));
            FileCopyUtils.copy(new File(testResources.getURI().getPath() + "/Patch5402.json"), new File(persistSt, "Patch5402.json"));

        } catch (IOException e) {
            fail("Unable to copy ServicesMetaData.json test file into testDb folder : " + e.getMessage());
        }
        PatchLogDetails pld = PatchLogDetails.builder()
                .target("dev-jhe")
                .patchPipelineTask("Build")
                .logText("Started")
                .datetime(new Date()).build();
        repo.savePatchLog("5401", pld);
    }

    @Test
    public void testSaveEmptyWithId() {
        Patch patch = Patch.builder().patchNumber("SomeUnqiueNumber1").build();
        patchClient.save(patch);
        Patch result = patchClient.findById("SomeUnqiueNumber1");
        assertNotNull(result);
        assertEquals(patch, result);
    }

    @Test
    public void testSavePatchLog() {
        Patch p = Patch.builder().patchNumber("anotherUniqueId").build();
        patchClient.save(p);
        PatchLogDetails pld = PatchLogDetails.builder()
                .target("dev-jhe")
                .patchPipelineTask("Build")
                .logText("Started")
                .datetime(new Date()).build();
        try {
            patchClient.log("anotherUniqueId", pld);
            fail();
        } catch (UnsupportedOperationException ex) {
            assertEquals(ex.getMessage(), "Logging patch activity not supported");
        }
    }

    @Test
    public void testSaveEmptyWithIdAndRemove() {
        Patch patch = Patch.builder().patchNumber("SomeUnqiueNumber2").build();
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
        Patch patch = Patch.builder().build();
        try {
            patchClient.save(patch);
            fail();

        } catch (AssertionError e){
            throw e;
        }
        catch (Throwable e) {
            // TODO Detail , Exception Handling
            // Ok
        }
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

        DBPatch dbPatch = DBPatch.builder().dbPatchBranch("SomePatchBranch").build();
        dbPatch.addDbObject(DbObject.builder()
                .fileName("FileName1")
                .filePath("FilePath1").build());
        dbPatch.addDbObject(DbObject.builder()
                .fileName("FileName2")
                .filePath("FilePath2").build());

        Patch patch = Patch.builder().patchNumber("SomeUnqiueNumber3")
                .services(Lists.newArrayList(service))
                .dbPatch(dbPatch).build();
        patchClient.save(patch);
        Patch result = patchClient.findById("SomeUnqiueNumber3");
        assertNotNull(result);
        assertEquals(patch, result);
    }

    @Test
    public void testFindByIds() {
        List<Patch> patches = patchClient.findByIds(Lists.newArrayList("5401", "5402"));
        assertEquals(2, patches.size());
    }

    @Test
    public void testFindPatchLogById() {
        PatchLog pl = patchClient.findPatchLogById("5401");
        assertNotNull(pl);
    }

    @Test
    public void testFindWithObjectName() {
        Patch p1 = Patch.builder().patchNumber("p1").build();
        Patch p2 = Patch.builder().patchNumber("p2").build();
        patchClient.save(p1);
        patchClient.save(p2);
        assertNotNull(patchClient.findById("p1"));
        assertNotNull(patchClient.findById("p2"));

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
        patchClient.save(p1Updated);
        patchClient.save(p2Updated);
        assertEquals(2, patchClient.findById("p1").retrieveAllArtifactsToPatch().size());
        assertEquals(2, patchClient.findById("p2").retrieveAllArtifactsToPatch().size());
        assertEquals(1, patchClient.findWithObjectName("ma1").size());
        assertEquals(1, patchClient.findWithObjectName("ma2").size());
        assertEquals(2, patchClient.findWithObjectName("ma3").size());
        assertEquals(0, patchClient.findWithObjectName("wrongName").size());
        assertEquals(1, patchClient.findWithObjectName("test-db2").size());
    }

    @Test
    public void testListOnDemandTargets() {
        List<String> onDemandTargets = patchClient.listOnDemandTargets();
        assertEquals(4, onDemandTargets.size());
        assertTrue(onDemandTargets.contains("DEV-CHEI212"));
        assertTrue(onDemandTargets.contains("DEV-CHEI211"));
        assertTrue(onDemandTargets.contains("DEV-CM"));
        assertTrue(onDemandTargets.contains("DEV-JHE"));
    }

    @Test
    public void testOnDemandInstallation() {
        // JHE : Whaou :) .... but allows to validate that parameter are correctly passed
        OnDemandParameter p = OnDemandParameter.builder().patchNumber("5401").target("DEV-JHE").build();
        patchClient.startOnDemandInstallation(p);
    }

}
