package com.apgsga.microservice.patch.core.impl.jenkins.test;

import com.apgsga.microservice.patch.api.*;
import com.apgsga.microservice.patch.core.impl.SimplePatchContainerBean;
import com.apgsga.microservice.patch.core.impl.jenkins.InstallDbObjectsInfos;
import com.apgsga.microservice.patch.core.impl.jenkins.JenkinsClient;
import com.apgsga.microservice.patch.core.impl.jenkins.JenkinsPipelinePreprocessor;
import com.apgsga.microservice.patch.core.impl.jenkins.PackagerInfo;
import com.apgsga.microservice.patch.server.MicroPatchServer;
import com.google.common.collect.Sets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.assertj.core.util.Lists;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestPropertySource(locations = "application-test.properties")
@SpringBootTest(classes = MicroPatchServer.class)
@ActiveProfiles("test,mock,mockMavenRepo,patchOMock,mockDocker")
public class TestJenkinsPipelinePreprocessor {

    protected final Log LOGGER = LogFactory.getLog(getClass());

    @Value("${json.meta.info.db.location}")
    private String metaInfoDb;

    @Value("${json.db.location:target/testdb}")
    private String dbLocation;

    @Autowired
    @Qualifier("patchPersistence")
    private PatchPersistence repo;

    @Autowired
    JenkinsPipelinePreprocessor preprocessor;

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
    public void testRetrieveStagesTargetAsCSV() {
        String s = preprocessor.retrieveStagesTargetAsCSV();
        Assert.assertTrue(s.toLowerCase().contains("informatiktest"));
        Assert.assertTrue(s.toLowerCase().contains("produktion"));
        Assert.assertTrue(s.toLowerCase().contains("anwendertest"));
        Assert.assertFalse(s.toLowerCase().contains("entwicklung"));
        Assert.assertEquals("informatiktest,produktion,anwendertest".length(),s.length());
    }

    @Test
    public void testRetrieveTargetForStageName() {
        Assert.assertEquals("DEV-CHEI212",preprocessor.retrieveTargetForStageName("Entwicklung"));
        Assert.assertEquals("DEV-CHEI211",preprocessor.retrieveTargetForStageName("Informatiktest"));
        Assert.assertEquals("DEV-CHTI211",preprocessor.retrieveTargetForStageName("Anwendertest"));
        Assert.assertEquals("DEV-CHPI211",preprocessor.retrieveTargetForStageName("Produktion"));
    }

    @Test
    public void testRetrievePatch() {
        repo.savePatch(Patch.builder().patchNumber("2222").build());
        Patch p = preprocessor.retrievePatch("2222");
        Assert.assertNotNull(p);
        Assert.assertEquals("2222",p.getPatchNumber());
    }

    @Test
    public void testRetrieveDbZipNamesWithoutDbObjects() {
        DBPatch dbp1 = DBPatch.builder().dbPatchBranch("1234_cvs_branch").build();
        Patch p1 = Patch.builder().patchNumber("1234").dbPatch(dbp1).build();
        DBPatch dbp2 = DBPatch.builder().dbPatchBranch("2345_cvs_branch").build();
        Patch p2 = Patch.builder().patchNumber("2345").dbPatch(dbp2).build();
        repo.savePatch(p1);
        repo.savePatch(p2);
        List<String> zipNames = preprocessor.retrieveDbZipNames(Sets.newHashSet("1234", "2345"), "DEV-CHEI211");
        Assert.assertTrue(zipNames.isEmpty());
    }

    @Test
    public void testRetrieveDbZipNamesWithDbObjects() {
        List<DbObject> dbo1 = Lists.newArrayList(DbObject.builder().moduleName("m1").fileName("f1").filePath("p1").build());
        DBPatch dbp1 = DBPatch.builder().dbPatchBranch("1234_cvs_branch").dbObjects(dbo1).build();
        Patch p1 = Patch.builder().patchNumber("1234").dbPatch(dbp1).build();
        List<DbObject> dbo2 = Lists.newArrayList(DbObject.builder().moduleName("m2").fileName("f2").filePath("p2").build());
        DBPatch dbp2 = DBPatch.builder().dbPatchBranch("2345_cvs_branch").dbObjects(dbo2).build();
        Patch p2 = Patch.builder().patchNumber("2345").dbPatch(dbp2).build();
        DBPatch dbp3 = DBPatch.builder().dbPatchBranch("3456_cvs_branch").build();
        Patch p3 = Patch.builder().patchNumber("3456").dbPatch(dbp3).build();
        repo.savePatch(p1);
        repo.savePatch(p2);
        repo.savePatch(p3);
        List<String> zipNames = preprocessor.retrieveDbZipNames(Sets.newHashSet("1234","2345","3456"), "DEV-CHEI211");
        Assert.assertEquals(2,zipNames.size());
        Assert.assertTrue(zipNames.contains("1234_cvs_branch_DEV-CHEI211.zip"));
        Assert.assertTrue(zipNames.contains("2345_cvs_branch_DEV-CHEI211.zip"));
    }

    @Test
    public void testRetrieveDbZipNamesForNonConfiguredTarget() {
        List<DbObject> dbo1 = Lists.newArrayList(DbObject.builder().moduleName("m1").fileName("f1").filePath("p1").build());
        DBPatch dbp1 = DBPatch.builder().dbPatchBranch("1234_cvs_branch").dbObjects(dbo1).build();
        Patch p1 = Patch.builder().patchNumber("1234").dbPatch(dbp1).build();
        List<DbObject> dbo2 = Lists.newArrayList(DbObject.builder().moduleName("m2").fileName("f2").filePath("p2").build());
        DBPatch dbp2 = DBPatch.builder().dbPatchBranch("2345_cvs_branch").dbObjects(dbo2).build();
        Patch p2 = Patch.builder().patchNumber("2345").dbPatch(dbp2).build();
        DBPatch dbp3 = DBPatch.builder().dbPatchBranch("3456_cvs_branch").build();
        Patch p3 = Patch.builder().patchNumber("3456").dbPatch(dbp3).build();
        repo.savePatch(p1);
        repo.savePatch(p2);
        repo.savePatch(p3);
        List<String> zipNames = preprocessor.retrieveDbZipNames(Sets.newHashSet("1234","2345","3456"), "DEV-DUMMY");
        Assert.assertTrue(zipNames.isEmpty());
    }

    @Test
    public void testNeedInstallDbPatchForWithoutDbObjects() {
        DBPatch dbp1 = DBPatch.builder().dbPatchBranch("1234_cvs_branch").build();
        Patch p1 = Patch.builder().patchNumber("1234").dbPatch(dbp1).build();
        DBPatch dbp2 = DBPatch.builder().dbPatchBranch("2345_cvs_branch").build();
        Patch p2 = Patch.builder().patchNumber("2345").dbPatch(dbp2).build();
        repo.savePatch(p1);
        repo.savePatch(p2);
        Assert.assertFalse(preprocessor.needInstallDbPatchFor(Sets.newHashSet("1234", "2345"), "DEV-CHEI211"));
    }

    @Test
    public void testNeedInstallDbPatchForWithDbObjects() {
        List<DbObject> dbo1 = Lists.newArrayList(DbObject.builder().moduleName("m1").fileName("f1").filePath("p1").build());
        DBPatch dbp1 = DBPatch.builder().dbPatchBranch("1234_cvs_branch").dbObjects(dbo1).build();
        Patch p1 = Patch.builder().patchNumber("1234").dbPatch(dbp1).build();
        List<DbObject> dbo2 = Lists.newArrayList(DbObject.builder().moduleName("m2").fileName("f2").filePath("p2").build());
        DBPatch dbp2 = DBPatch.builder().dbPatchBranch("2345_cvs_branch").dbObjects(dbo2).build();
        Patch p2 = Patch.builder().patchNumber("2345").dbPatch(dbp2).build();
        DBPatch dbp3 = DBPatch.builder().dbPatchBranch("3456_cvs_branch").build();
        Patch p3 = Patch.builder().patchNumber("3456").dbPatch(dbp3).build();
        repo.savePatch(p1);
        repo.savePatch(p2);
        repo.savePatch(p3);
        Assert.assertTrue(preprocessor.needInstallDbPatchFor(Sets.newHashSet("1234","2345","3456"), "DEV-CHEI211"));
    }

    @Test
    public void testNeedInstallDbPatchForForNonConfiguredTarget() {
        List<DbObject> dbo1 = Lists.newArrayList(DbObject.builder().moduleName("m1").fileName("f1").filePath("p1").build());
        DBPatch dbp1 = DBPatch.builder().dbPatchBranch("1234_cvs_branch").dbObjects(dbo1).build();
        Patch p1 = Patch.builder().patchNumber("1234").dbPatch(dbp1).build();
        List<DbObject> dbo2 = Lists.newArrayList(DbObject.builder().moduleName("m2").fileName("f2").filePath("p2").build());
        DBPatch dbp2 = DBPatch.builder().dbPatchBranch("2345_cvs_branch").dbObjects(dbo2).build();
        Patch p2 = Patch.builder().patchNumber("2345").dbPatch(dbp2).build();
        DBPatch dbp3 = DBPatch.builder().dbPatchBranch("3456_cvs_branch").build();
        Patch p3 = Patch.builder().patchNumber("3456").dbPatch(dbp3).build();
        repo.savePatch(p1);
        repo.savePatch(p2);
        repo.savePatch(p3);
        Assert.assertFalse(preprocessor.needInstallDbPatchFor(Sets.newHashSet("1234","2345","3456"), "DEV-DUMMY"));
    }

    @Test
    public void testNeedInstallDockerServicesFor() {
        Patch p1 = Patch.builder().patchNumber("1234").dockerServices(Lists.newArrayList("docker-service-1")).build();
        Patch p2 = Patch.builder().patchNumber("2345").dbPatch(DBPatch.builder().dbPatchBranch("test").build()).build();
        repo.savePatch(p1);
        repo.savePatch(p2);
        Assert.assertTrue(preprocessor.needInstallDockerServicesFor(Sets.newHashSet("1234","2345")));
        Assert.assertTrue(preprocessor.needInstallDockerServicesFor(Sets.newHashSet("1234")));
        Assert.assertFalse(preprocessor.needInstallDockerServicesFor(Sets.newHashSet("2345")));
    }

    @Test
    public void testRetrievePackagerInfoForTargetInStageMapping() {
        List<Service> services1 = Lists.newArrayList(Service.builder().serviceName("it21").build());
        Patch p1 = Patch.builder().patchNumber("1234").services(services1).build();
        List<Service> services2 = Lists.newArrayList(Service.builder().serviceName("digiflex").build());
        Patch p2 = Patch.builder().patchNumber("2345").services(services2).build();
        repo.savePatch(p1);
        repo.savePatch(p2);
        List<PackagerInfo> packagers = preprocessor.retrievePackagerInfoFor(Sets.newHashSet("1234", "2345"), "DEV-CHEI211");
        Assert.assertFalse(packagers.isEmpty());
        Assert.assertEquals(4,packagers.size());
    }

    @Test
    public void testRetrievePackagerInfoForConfiguredTargetNotInStageMapping() {
        ServiceMetaData it21Metadata = repo.getServiceMetaDataByName("it21");
        List<Service> services1 = Lists.newArrayList(Service.builder().serviceName("it21").serviceMetaData(it21Metadata).build());
        Patch p1 = Patch.builder().patchNumber("1234").services(services1).build();
        ServiceMetaData digiflexMetadata = repo.getServiceMetaDataByName("digiflex");
        List<Service> services2 = Lists.newArrayList(Service.builder().serviceName("digiflex").serviceMetaData(digiflexMetadata).build());
        Patch p2 = Patch.builder().patchNumber("2345").services(services2).build();
        repo.savePatch(p1);
        repo.savePatch(p2);
        List<PackagerInfo> packagers = preprocessor.retrievePackagerInfoFor(Sets.newHashSet("1234", "2345"), "DEV-JHE");
        Assert.assertFalse(packagers.isEmpty());
        Assert.assertEquals(4,packagers.size());
    }

    @Test
    public void testRetrievePackagerInfoForNonConfiguredTargetNotInStageMapping() {
        ServiceMetaData it21Metadata = repo.getServiceMetaDataByName("it21");
        List<Service> services1 = Lists.newArrayList(Service.builder().serviceName("it21").serviceMetaData(it21Metadata).build());
        Patch p1 = Patch.builder().patchNumber("1234").services(services1).build();
        ServiceMetaData digiflexMetadata = repo.getServiceMetaDataByName("digiflex");
        List<Service> services2 = Lists.newArrayList(Service.builder().serviceName("digiflex").serviceMetaData(digiflexMetadata).build());
        Patch p2 = Patch.builder().patchNumber("2345").services(services2).build();
        repo.savePatch(p1);
        repo.savePatch(p2);
        List<PackagerInfo> packagers = preprocessor.retrievePackagerInfoFor(Sets.newHashSet("1234", "2345"), "DEV-DUMMY");
        Assert.assertTrue(packagers.isEmpty());
    }

    @Test
    public void testRetrieveDbObjectInfoForTargetInStageMapping() {
        DBPatch db1 = DBPatch.builder().dbObjects(Lists.newArrayList(DbObject.builder().filePath("p1").fileName("n1").moduleName("m1").build())).build();
        Patch p1 = Patch.builder().patchNumber("1234").dbPatch(db1).build();
        DBPatch db2 = DBPatch.builder().dbObjects(Lists.newArrayList(DbObject.builder().filePath("p2").fileName("n2").moduleName("m2").build())).build();
        Patch p2 = Patch.builder().patchNumber("2345").dbPatch(db2).build();
        repo.savePatch(p1);
        repo.savePatch(p2);
        Map<String, InstallDbObjectsInfos> infoDbObjects = preprocessor.retrieveDbObjectInfoFor(Sets.newHashSet("1234", "2345"), "DEV-CHEI211");
        Assert.assertNotNull(infoDbObjects);
        Assert.assertEquals(2,infoDbObjects.keySet().size());
        Assert.assertTrue(infoDbObjects.keySet().contains("1234"));
        Assert.assertTrue(infoDbObjects.keySet().contains("2345"));
        Assert.assertEquals("m1",infoDbObjects.get("1234").dbObjectsModuleNames.toArray()[0]);
        Assert.assertEquals("m2",infoDbObjects.get("2345").dbObjectsModuleNames.toArray()[0]);
    }

    @Test
    public void testRetrieveDbObjectInfoForConfiguredTargetNotInStageMapping() {
        DBPatch db1 = DBPatch.builder().dbObjects(Lists.newArrayList(DbObject.builder().filePath("p1").fileName("n1").moduleName("m1").build())).build();
        Patch p1 = Patch.builder().patchNumber("1234").dbPatch(db1).build();
        DBPatch db2 = DBPatch.builder().dbObjects(Lists.newArrayList(DbObject.builder().filePath("p2").fileName("n2").moduleName("m2").build())).build();
        Patch p2 = Patch.builder().patchNumber("2345").dbPatch(db2).build();
        repo.savePatch(p1);
        repo.savePatch(p2);
        Map<String, InstallDbObjectsInfos> infoDbObjects = preprocessor.retrieveDbObjectInfoFor(Sets.newHashSet("1234", "2345"), "DEV-JHE");
        Assert.assertNotNull(infoDbObjects);
        Assert.assertEquals(2,infoDbObjects.keySet().size());
        Assert.assertTrue(infoDbObjects.keySet().contains("1234"));
        Assert.assertTrue(infoDbObjects.keySet().contains("2345"));
        Assert.assertEquals("m1",infoDbObjects.get("1234").dbObjectsModuleNames.toArray()[0]);
        Assert.assertEquals("m2",infoDbObjects.get("2345").dbObjectsModuleNames.toArray()[0]);
    }

    @Test
    public void testRetrieveDbObjectInfoForNonConfiguredTargetNotInStageMapping() {
        DBPatch db1 = DBPatch.builder().dbObjects(Lists.newArrayList(DbObject.builder().filePath("p1").fileName("n1").moduleName("m1").build())).build();
        Patch p1 = Patch.builder().patchNumber("1234").dbPatch(db1).build();
        DBPatch db2 = DBPatch.builder().dbObjects(Lists.newArrayList(DbObject.builder().filePath("p2").fileName("n2").moduleName("m2").build())).build();
        Patch p2 = Patch.builder().patchNumber("2345").dbPatch(db2).build();
        repo.savePatch(p1);
        repo.savePatch(p2);
        Map<String, InstallDbObjectsInfos> infoDbObjects = preprocessor.retrieveDbObjectInfoFor(Sets.newHashSet("1234", "2345"), "DEV-DUMMY");
        Assert.assertTrue(infoDbObjects.keySet().isEmpty());
    }

    @Test
    public void testRetrieveDbDeployInstallerHostForTargetInStageMapping() {
        Assert.assertNotNull(preprocessor.retrieveDbDeployInstallerHost("DEV-CHEI211"));
        Assert.assertEquals("dev-chei211.apgsga.ch",preprocessor.retrieveDbDeployInstallerHost("DEV-CHEI211"));
    }

    @Test
    public void testRetrieveDbDeployInstallerHostForConfiguredTargetNotInStageMapping() {
        Assert.assertNotNull(preprocessor.retrieveDbDeployInstallerHost("DEV-JHE"));
        Assert.assertEquals("dev-chei211.apgsga.ch",preprocessor.retrieveDbDeployInstallerHost("DEV-JHE"));
    }

    @Test
    public void testRetrieveDbDeployInstallerHostForNonConfiguredTargetNotInStageMapping() {
        Assert.assertNull(preprocessor.retrieveDbDeployInstallerHost("DEV-DUMMY"));
    }

    @Test
    public void testIsTargetPartOfStageMapping() {
        Assert.assertTrue(preprocessor.isTargetPartOfStageMapping("DEV-CHPI211"));
        Assert.assertTrue(preprocessor.isTargetPartOfStageMapping("DEV-CHEI211"));
        Assert.assertFalse(preprocessor.isTargetPartOfStageMapping("DEV-JHE"));
        Assert.assertFalse(preprocessor.isTargetPartOfStageMapping("DEV-DUMMY"));
    }

    @Test
    public void testIsDbConfiguredFor() {
        Assert.assertTrue(preprocessor.isDbConfiguredFor("DEV-CHPI211"));
        Assert.assertTrue(preprocessor.isDbConfiguredFor("DEV-CHEI211"));
        Assert.assertFalse(preprocessor.isDbConfiguredFor("DEV-DUMMY"));
    }

    @Test
    public void testReduceOnlyServicesConfiguredForTargetForTargetInStageMapping() {
        Service s1 = Service.builder().serviceName("it21").build();
        Service s2 = Service.builder().serviceName("digiflex").build();
        Service s3 = Service.builder().serviceName("info-display").build();
        List<Service> services = Lists.newArrayList(s1,s2,s3);
        List<Service> devChei211Result = preprocessor.reduceOnlyServicesConfiguredForTarget(services, "DEV-CHEI211");
        Assert.assertEquals(3,devChei211Result.size());
        Assert.assertTrue(devChei211Result.stream().map(r -> r.getServiceName()).collect(Collectors.toList()).contains("it21"));
        Assert.assertTrue(devChei211Result.stream().map(r -> r.getServiceName()).collect(Collectors.toList()).contains("digiflex"));
        Assert.assertTrue(devChei211Result.stream().map(r -> r.getServiceName()).collect(Collectors.toList()).contains("info-display"));
    }

    @Test
    public void testReduceOnlyServicesConfiguredForConfiguredTargetNotInStageMapping() {
        Service s1 = Service.builder().serviceName("it21").serviceMetaData(repo.getServiceMetaDataByName("it21")).build();
        Service s2 = Service.builder().serviceName("digiflex").serviceMetaData(repo.getServiceMetaDataByName("digiflex")).build();
        Service s3 = Service.builder().serviceName("info-display").serviceMetaData(repo.getServiceMetaDataByName("info-display")).build();
        List<Service> services = Lists.newArrayList(s1,s2,s3);
        List<Service> devChei211Result = preprocessor.reduceOnlyServicesConfiguredForTarget(services, "DEV-JHE");
        Assert.assertEquals(2,devChei211Result.size());
        Assert.assertTrue(devChei211Result.stream().map(r -> r.getServiceName()).collect(Collectors.toList()).contains("it21"));
        Assert.assertTrue(devChei211Result.stream().map(r -> r.getServiceName()).collect(Collectors.toList()).contains("digiflex"));
    }

    @Test
    public void testReduceOnlyServicesConfiguredForNonConfiguredTargetNotInStageMapping() {
        Service s1 = Service.builder().serviceName("it21").serviceMetaData(repo.getServiceMetaDataByName("it21")).build();
        Service s2 = Service.builder().serviceName("digiflex").serviceMetaData(repo.getServiceMetaDataByName("digiflex")).build();
        Service s3 = Service.builder().serviceName("info-display").serviceMetaData(repo.getServiceMetaDataByName("info-display")).build();
        List<Service> services = Lists.newArrayList(s1,s2,s3);
        List<Service> devChei211Result = preprocessor.reduceOnlyServicesConfiguredForTarget(services, "DEV-DUMMY");
        Assert.assertEquals(0,devChei211Result.size());
    }
}
