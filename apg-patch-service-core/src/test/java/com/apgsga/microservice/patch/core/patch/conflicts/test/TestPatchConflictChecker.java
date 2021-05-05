package com.apgsga.microservice.patch.core.patch.conflicts.test;

import com.apgsga.microservice.patch.api.*;
import com.apgsga.microservice.patch.core.patch.conflicts.PatchConflict;
import com.apgsga.microservice.patch.core.patch.conflicts.PatchConflictsCheckerImpl;
import com.apgsga.microservice.patch.core.patch.conflicts.PatchConflictsChecker;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestPatchConflictChecker {

    @Test
    public void testConflictDockerServicesForTwoPatches() {
        Patch p1 = Patch.builder().patchNumber("1234").dockerServices(Lists.newArrayList("dockerService")).build();
        Patch p2 = Patch.builder().patchNumber("2345").dockerServices(Lists.newArrayList("dockerService")).build();
        PatchConflictsChecker checker = PatchConflictsCheckerImpl.create();
        checker.addPatch(p1);
        checker.addPatch(p2);
        List<PatchConflict> patchConflicts = checker.checkConflicts();
        Assert.assertTrue(patchConflicts != null);
        Assert.assertTrue(!patchConflicts.isEmpty());
        Assert.assertTrue(patchConflicts.size() == 1);
        Assert.assertTrue(patchConflicts.get(0).getDockerServices().size() == 1);
        Assert.assertTrue(patchConflicts.get(0).getDockerServices().get(0).equals("dockerService"));
        Assert.assertTrue(patchConflicts.get(0).getDbObjects().isEmpty());
        Assert.assertTrue(patchConflicts.get(0).getServiceWithMavenArtifacts().isEmpty());
        Assert.assertTrue(Lists.newArrayList("1234","2345").contains(patchConflicts.get(0).getP1().getPatchNumber()));
        Assert.assertTrue(Lists.newArrayList("1234","2345").contains(patchConflicts.get(0).getP2().getPatchNumber()));
        Assert.assertFalse(patchConflicts.get(0).getP1().getPatchNumber().equals(patchConflicts.get(0).getP2().getPatchNumber()));
    }

    @Test
    public void testConflictDockerServicesForMultiplePatches() {
        Patch p1 = Patch.builder().patchNumber("1234").dockerServices(Lists.newArrayList("dockerService")).build();
        Patch p2 = Patch.builder().patchNumber("2345").dockerServices(Lists.newArrayList("dockerService_2")).build();
        Patch p3 = Patch.builder().patchNumber("3456").dockerServices(Lists.newArrayList("dockerService_2")).build();
        Patch p4 = Patch.builder().patchNumber("4567").dockerServices(Lists.newArrayList("dockerService")).build();
        PatchConflictsChecker checker = PatchConflictsCheckerImpl.create();
        checker.addPatch(p1);
        checker.addPatch(p2);
        checker.addPatch(p3);
        checker.addPatch(p4);
        List<PatchConflict> patchConflicts = checker.checkConflicts();
        Assert.assertTrue(patchConflicts != null);
        Assert.assertTrue(!patchConflicts.isEmpty());
        Assert.assertTrue(patchConflicts.size() == 2);
        Assert.assertTrue(patchConflicts.get(0).getDockerServices().size() == 1);
        Assert.assertTrue(patchConflicts.get(1).getDockerServices().size() == 1);

        for(PatchConflict pc : patchConflicts) {
            if(pc.getP1().getPatchNumber().equals(p1.getPatchNumber()) || pc.getP2().getPatchNumber().equals(p1.getPatchNumber())) {
                Assert.assertTrue(Lists.newArrayList(p1.getPatchNumber(),p4.getPatchNumber()).contains(pc.getP1().getPatchNumber()));
                Assert.assertTrue(Lists.newArrayList(p1.getPatchNumber(),p4.getPatchNumber()).contains(pc.getP2().getPatchNumber()));
                Assert.assertTrue(pc.getDockerServices().size() == 1);
                Assert.assertTrue(pc.getDockerServices().get(0).equals("dockerService"));
                Assert.assertTrue(pc.getServiceWithMavenArtifacts().isEmpty());
                Assert.assertTrue(pc.getDbObjects().isEmpty());
            }
            else {
                Assert.assertTrue(Lists.newArrayList(p2.getPatchNumber(),p3.getPatchNumber()).contains(pc.getP1().getPatchNumber()));
                Assert.assertTrue(Lists.newArrayList(p2.getPatchNumber(),p3.getPatchNumber()).contains(pc.getP2().getPatchNumber()));
                Assert.assertTrue(pc.getDockerServices().size() == 1);
                Assert.assertTrue(pc.getDockerServices().get(0).equals("dockerService_2"));
                Assert.assertTrue(pc.getServiceWithMavenArtifacts().isEmpty());
                Assert.assertTrue(pc.getDbObjects().isEmpty());
            }
        }
    }

    @Test
    public void testConflictDbObjectForTwoPatches() {

        List<DbObject> dbo1 = Lists.newArrayList();
        dbo1.add(DbObject.builder().moduleName("test.modules").fileName("testFileName.sql").filePath("test/file/path").build());
        dbo1.add(DbObject.builder().moduleName("test.modules.2").fileName("testFileName_2.sql").filePath("test/file/path_2").build());
        DBPatch dbp1 = DBPatch.builder().dbObjects(dbo1).build();
        Patch p1 = Patch.builder().patchNumber("1234").dbPatch(dbp1).build();

        List<DbObject> dbo2 = Lists.newArrayList();
        dbo2.add(DbObject.builder().moduleName("test.modules").fileName("testFileName.sql").filePath("test/file/path").build());
        DBPatch dbp2 = DBPatch.builder().dbObjects(dbo2).build();
        Patch p2 = Patch.builder().patchNumber("2345").dbPatch(dbp2).build();

        PatchConflictsChecker conflictsChecker = PatchConflictsCheckerImpl.create();
        conflictsChecker.addPatch(p1);
        conflictsChecker.addPatch(p2);

        List<PatchConflict> patchConflicts = conflictsChecker.checkConflicts();
        Assert.assertTrue(patchConflicts.size() == 1);
        Assert.assertTrue(patchConflicts.get(0).getDockerServices().isEmpty());
        Assert.assertTrue(patchConflicts.get(0).getServiceWithMavenArtifacts().isEmpty());
        Assert.assertTrue(patchConflicts.get(0).getDbObjects().size() == 1);
        Assert.assertTrue(patchConflicts.get(0).getDbObjects().get(0).getModuleName().equals("test.modules"));
        Assert.assertTrue(patchConflicts.get(0).getDbObjects().get(0).getFileName().equals("testFileName.sql"));
        Assert.assertTrue(patchConflicts.get(0).getDbObjects().get(0).getFilePath().equals("test/file/path"));
        Assert.assertTrue(Lists.newArrayList(p1.getPatchNumber(),p2.getPatchNumber()).contains(patchConflicts.get(0).getP1().getPatchNumber()));
        Assert.assertTrue(Lists.newArrayList(p1.getPatchNumber(),p2.getPatchNumber()).contains(patchConflicts.get(0).getP2().getPatchNumber()));
    }

    @Test
    public void testConflictDbObjectForMultiplePatches() {

        List<DbObject> dbo1 = Lists.newArrayList();
        dbo1.add(DbObject.builder().moduleName("test.modules").fileName("testFileName.sql").filePath("test/file/path").build());
        dbo1.add(DbObject.builder().moduleName("test.modules.2").fileName("testFileName_2.sql").filePath("test/file/path_2").build());
        DBPatch dbp1 = DBPatch.builder().dbObjects(dbo1).build();
        Patch p1 = Patch.builder().patchNumber("1234").dbPatch(dbp1).build();

        List<DbObject> dbo2 = Lists.newArrayList();
        dbo2.add(DbObject.builder().moduleName("test.modules.another").fileName("testFileName_another.sql").filePath("test/file/path/another").build());
        DBPatch dbp2 = DBPatch.builder().dbObjects(dbo2).build();
        Patch p2 = Patch.builder().patchNumber("2345").dbPatch(dbp2).build();

        List<DbObject> dbo3 = Lists.newArrayList();
        dbo3.add(DbObject.builder().moduleName("test.modules.3").fileName("testFileName_3.sql").filePath("test/file/path_3").build());
        DBPatch dbp3 = DBPatch.builder().dbObjects(dbo3).build();
        Patch p3 = Patch.builder().patchNumber("3456").dbPatch(dbp3).build();

        List<DbObject> dbo4 = Lists.newArrayList();
        dbo4.add(DbObject.builder().moduleName("test.modules.4").fileName("testFileName_4.sql").filePath("test/file/path_4").build());
        dbo4.add(DbObject.builder().moduleName("test.modules").fileName("testFileName.sql").filePath("test/file/path").build());
        DBPatch dbp4 = DBPatch.builder().dbObjects(dbo4).build();
        Patch p4 = Patch.builder().patchNumber("4567").dbPatch(dbp4).build();

        PatchConflictsChecker conflictsChecker = PatchConflictsCheckerImpl.create();
        conflictsChecker.addPatch(p1);
        conflictsChecker.addPatch(p2);
        conflictsChecker.addPatch(p3);
        conflictsChecker.addPatch(p4);

        List<PatchConflict> patchConflicts = conflictsChecker.checkConflicts();
        Assert.assertTrue(patchConflicts.size() == 1);
        Assert.assertTrue(patchConflicts.get(0).getDockerServices().isEmpty());
        Assert.assertTrue(patchConflicts.get(0).getServiceWithMavenArtifacts().isEmpty());
        Assert.assertTrue(patchConflicts.get(0).getDbObjects().size() == 1);
        Assert.assertTrue(patchConflicts.get(0).getDbObjects().get(0).getModuleName().equals("test.modules"));
        Assert.assertTrue(patchConflicts.get(0).getDbObjects().get(0).getFileName().equals("testFileName.sql"));
        Assert.assertTrue(patchConflicts.get(0).getDbObjects().get(0).getFilePath().equals("test/file/path"));
        Assert.assertTrue(Lists.newArrayList(p1.getPatchNumber(),p4.getPatchNumber()).contains(patchConflicts.get(0).getP1().getPatchNumber()));
        Assert.assertTrue(Lists.newArrayList(p1.getPatchNumber(),p4.getPatchNumber()).contains(patchConflicts.get(0).getP2().getPatchNumber()));
    }

    @Test
    public void testConflictSameMavenArtifactForSameServiceForTwoPatches() {
        List<MavenArtifact> artifacts1 = Lists.newArrayList(MavenArtifact.builder().artifactId("art").groupId("grp").name("art.grp").version("ver").build());
        List<Service> s1 = Lists.newArrayList(Service.builder().serviceName("s1").artifactsToPatch(artifacts1).build());
        Patch p1 = Patch.builder().patchNumber("1234").services(s1).build();

        List<MavenArtifact> artifacts2 = Lists.newArrayList(MavenArtifact.builder().artifactId("art").groupId("grp").name("art.grp").version("ver").build());
        List<Service> s2 = Lists.newArrayList(Service.builder().serviceName("s1").artifactsToPatch(artifacts2).build());
        Patch p2 = Patch.builder().patchNumber("2345").services(s2).build();

        PatchConflictsChecker conflictsChecker = PatchConflictsCheckerImpl.create();
        conflictsChecker.addPatch(p1);
        conflictsChecker.addPatch(p2);

        List<PatchConflict> patchConflicts = conflictsChecker.checkConflicts();
        Assert.assertTrue(patchConflicts.size() == 1);
        Assert.assertTrue(patchConflicts.get(0).getDbObjects().isEmpty());
        Assert.assertTrue(patchConflicts.get(0).getDockerServices().isEmpty());
        Assert.assertTrue(patchConflicts.get(0).getServiceWithMavenArtifacts().size() == 1);
        Assert.assertTrue(patchConflicts.get(0).getServiceWithMavenArtifacts().get("s1").size() == 1);
        Assert.assertTrue(patchConflicts.get(0).getServiceWithMavenArtifacts().get("s1").get(0).getArtifactId().equals("art"));
        Assert.assertTrue(patchConflicts.get(0).getServiceWithMavenArtifacts().get("s1").get(0).getGroupId().equals("grp"));
        Assert.assertTrue(patchConflicts.get(0).getServiceWithMavenArtifacts().get("s1").get(0).getName().equals("art.grp"));
        Assert.assertTrue(patchConflicts.get(0).getServiceWithMavenArtifacts().get("s1").get(0).getVersion().equals("ver"));
        Assert.assertTrue(Lists.newArrayList(p1.getPatchNumber(),p2.getPatchNumber()).contains(patchConflicts.get(0).getP1().getPatchNumber()));
        Assert.assertTrue(Lists.newArrayList(p1.getPatchNumber(),p2.getPatchNumber()).contains(patchConflicts.get(0).getP2().getPatchNumber()));
    }

    @Test
    public void testConflictSameMavenArtifactForSameServiceForMultiplePatches() {
        List<MavenArtifact> artifacts1 = Lists.newArrayList(MavenArtifact.builder().artifactId("art").groupId("grp").name("art.grp").version("ver").build());
        List<Service> s1 = Lists.newArrayList(Service.builder().serviceName("s1").artifactsToPatch(artifacts1).build());
        Patch p1 = Patch.builder().patchNumber("1234").services(s1).build();

        List<MavenArtifact> artifacts2 = Lists.newArrayList(MavenArtifact.builder().artifactId("art").groupId("grp").name("art.grp").version("ver").build());
        List<Service> s2 = Lists.newArrayList(Service.builder().serviceName("s1").artifactsToPatch(artifacts2).build());
        Patch p2 = Patch.builder().patchNumber("2345").services(s2).build();

        List<MavenArtifact> artifacts3 = Lists.newArrayList(MavenArtifact.builder().artifactId("art_2").groupId("grp").name("art.grp").version("ver").build());
        List<Service> s3 = Lists.newArrayList(Service.builder().serviceName("s1").artifactsToPatch(artifacts3).build());
        Patch p3 = Patch.builder().patchNumber("3456").services(s3).build();

        List<MavenArtifact> artifacts4 = Lists.newArrayList(MavenArtifact.builder().artifactId("art_2").groupId("grp").name("art.grp").version("ver").build());
        List<Service> s4 = Lists.newArrayList(Service.builder().serviceName("s1").artifactsToPatch(artifacts4).build());
        Patch p4 = Patch.builder().patchNumber("3456").services(s4).build();

        PatchConflictsChecker conflictsChecker = PatchConflictsCheckerImpl.create();
        conflictsChecker.addPatch(p1);
        conflictsChecker.addPatch(p2);
        conflictsChecker.addPatch(p3);
        conflictsChecker.addPatch(p4);

        List<PatchConflict> patchConflicts = conflictsChecker.checkConflicts();
        Assert.assertTrue(patchConflicts.size() == 2);
        Assert.assertTrue(patchConflicts.get(0).getDbObjects().isEmpty());
        Assert.assertTrue(patchConflicts.get(0).getDockerServices().isEmpty());
        Assert.assertTrue(patchConflicts.get(1).getDbObjects().isEmpty());
        Assert.assertTrue(patchConflicts.get(1).getDockerServices().isEmpty());
        Assert.assertTrue(patchConflicts.get(0).getServiceWithMavenArtifacts().size() == 1);
        Assert.assertTrue(patchConflicts.get(0).getServiceWithMavenArtifacts().get("s1").size() == 1);
        Assert.assertTrue(patchConflicts.get(1).getServiceWithMavenArtifacts().size() == 1);
        Assert.assertTrue(patchConflicts.get(1).getServiceWithMavenArtifacts().get("s1").size() == 1);


        patchConflicts.forEach(pc -> {
            if(pc.getServiceWithMavenArtifacts().get("s1").get(0).getArtifactId().equals("art")) {
                Assert.assertTrue(pc.getServiceWithMavenArtifacts().get("s1").get(0).getArtifactId().equals("art"));
                Assert.assertTrue(pc.getServiceWithMavenArtifacts().get("s1").get(0).getGroupId().equals("grp"));
                Assert.assertTrue(pc.getServiceWithMavenArtifacts().get("s1").get(0).getName().equals("art.grp"));
                Assert.assertTrue(pc.getServiceWithMavenArtifacts().get("s1").get(0).getVersion().equals("ver"));
                Assert.assertTrue(Lists.newArrayList(p1.getPatchNumber(),p2.getPatchNumber()).contains(pc.getP1().getPatchNumber()));
                Assert.assertTrue(Lists.newArrayList(p1.getPatchNumber(),p2.getPatchNumber()).contains(pc.getP2().getPatchNumber()));
            }
            else {
                Assert.assertTrue(pc.getServiceWithMavenArtifacts().get("s1").get(0).getArtifactId().equals("art_2"));
                Assert.assertTrue(pc.getServiceWithMavenArtifacts().get("s1").get(0).getGroupId().equals("grp"));
                Assert.assertTrue(pc.getServiceWithMavenArtifacts().get("s1").get(0).getName().equals("art.grp"));
                Assert.assertTrue(pc.getServiceWithMavenArtifacts().get("s1").get(0).getVersion().equals("ver"));
                Assert.assertTrue(Lists.newArrayList(p3.getPatchNumber(),p4.getPatchNumber()).contains(pc.getP1().getPatchNumber()));
                Assert.assertTrue(Lists.newArrayList(p3.getPatchNumber(),p4.getPatchNumber()).contains(pc.getP2().getPatchNumber()));
            }
        });
    }

    @Test
    public void testConflictMultipleObjectsForMultiplePatch() {
        List<DbObject> dbo1 = Lists.newArrayList(DbObject.builder().moduleName("module_dbo1").fileName("filename_dbo1").filePath("patch_dbo1").build(),
                                                 DbObject.builder().moduleName("module_dbo1_b").fileName("filename_dbo1_b").filePath("patch_dbo1_b").build());
        DBPatch dbp1 = DBPatch.builder().dbObjects(dbo1).build();
        List<MavenArtifact> mavenArtifacts1 = Lists.newArrayList(MavenArtifact.builder().artifactId("art").groupId("grp").name("art-grp").version("ver").build(),
                                                                 MavenArtifact.builder().artifactId("art2").groupId("grp2").name("art-grp2").version("ver").build(),
                                                                 MavenArtifact.builder().artifactId("art3").groupId("grp").name("art-grp").version("ver").build());
        List<Service> services1 = Lists.newArrayList(Service.builder().serviceName("testService").artifactsToPatch(mavenArtifacts1).build());
        Patch p1 = Patch.builder().patchNumber("1").dockerServices(Lists.newArrayList("dockerService","dockerService_2","dockerService_3"))
                .dbPatch(dbp1)
                .services(services1)
                .build();

        List<DbObject> dbo2 = Lists.newArrayList(DbObject.builder().moduleName("module_dbo2").fileName("filename_dbo2").filePath("patch_dbo2").build(),
                DbObject.builder().moduleName("module_dbo2_b").fileName("filename_dbo2_b").filePath("patch_dbo2_b").build());
        DBPatch dbp2 = DBPatch.builder().dbObjects(dbo2).build();
        List<MavenArtifact> mavenArtifacts2 = Lists.newArrayList(MavenArtifact.builder().artifactId("art_2").groupId("grp_2").name("art-grp_2").version("ver").build(),
                MavenArtifact.builder().artifactId("art2_2").groupId("grp2_2").name("art-grp2_2").version("ver").build(),
                MavenArtifact.builder().artifactId("art3_2").groupId("grp").name("art-grp").version("ver").build());
        List<Service> services2 = Lists.newArrayList(Service.builder().serviceName("testService").artifactsToPatch(mavenArtifacts2).build());
        Patch p2 = Patch.builder().patchNumber("2").dockerServices(Lists.newArrayList("dockerService_2"))
                .dbPatch(dbp2)
                .services(services2)
                .build();

        List<DbObject> dbo3 = Lists.newArrayList(DbObject.builder().moduleName("module_dbo3").fileName("filename_dbo3").filePath("patch_dbo3").build());
        DBPatch dbp3 = DBPatch.builder().dbObjects(dbo3).build();
        List<MavenArtifact> mavenArtifacts3 = Lists.newArrayList(MavenArtifact.builder().artifactId("art_3").groupId("grp_3").name("art-grp_3").version("ver").build(),
                MavenArtifact.builder().artifactId("art2").groupId("grp2").name("art-grp2").version("ver").build(),
                MavenArtifact.builder().artifactId("art3_3").groupId("grp").name("art-grp").version("ver").build());
        List<Service> services3 = Lists.newArrayList(Service.builder().serviceName("testService").artifactsToPatch(mavenArtifacts3).build());
        Patch p3 = Patch.builder().patchNumber("3").dockerServices(Lists.newArrayList("dockerService","anotherService"))
                .dbPatch(dbp3)
                .services(services3)
                .build();

        List<DbObject> dbo4 = Lists.newArrayList(DbObject.builder().moduleName("module_dbo4").fileName("filename_dbo4").filePath("patch_dbo4").build());
        DBPatch dbp4 = DBPatch.builder().dbObjects(dbo4).build();
        Patch p4 = Patch.builder().patchNumber("4").dockerServices(Lists.newArrayList("dockerService_4"))
                .dbPatch(dbp4)
                .build();

        List<DbObject> dbo5 = Lists.newArrayList(DbObject.builder().moduleName("module_dbo2").fileName("filename_dbo2").filePath("patch_dbo2").build(),
                DbObject.builder().moduleName("module_dbo2_b").fileName("filename_dbo2_b").filePath("patch_dbo2_b").build());
        DBPatch dbp5 = DBPatch.builder().dbObjects(dbo2).build();
        Patch p5 = Patch.builder().patchNumber("5").dbPatch(dbp5).build();

        PatchConflictsChecker conflictsChecker = PatchConflictsCheckerImpl.create();
        conflictsChecker.addPatch(p1);
        conflictsChecker.addPatch(p2);
        conflictsChecker.addPatch(p3);
        conflictsChecker.addPatch(p4);
        conflictsChecker.addPatch(p5);
        List<PatchConflict> patchConflicts = conflictsChecker.checkConflicts();

        Assert.assertTrue("Size was " + patchConflicts.size(), patchConflicts.size() == 3);

        patchConflicts.forEach(pc -> {
            if(Lists.newArrayList("1","2").contains(pc.getP1().getPatchNumber()) && Lists.newArrayList("1","2").contains(pc.getP2().getPatchNumber())) {
                Assert.assertTrue(pc.getServiceWithMavenArtifacts().isEmpty());
                Assert.assertTrue(pc.getDbObjects().isEmpty());
                Assert.assertTrue(pc.getDockerServices().size() == 1);
                Assert.assertTrue(pc.getDockerServices().get(0).equals("dockerService_2"));
            } else if(Lists.newArrayList("1","3").contains(pc.getP1().getPatchNumber()) && Lists.newArrayList("1","3").contains(pc.getP2().getPatchNumber())) {
                Assert.assertTrue(pc.getDbObjects().isEmpty());
                Assert.assertTrue(pc.getDockerServices().size() == 1);
                Assert.assertTrue(pc.getDockerServices().get(0).equals("dockerService"));
                Assert.assertTrue(pc.getServiceWithMavenArtifacts().size() == 1);
                Assert.assertTrue(pc.getServiceWithMavenArtifacts().get("testService").size() == 1);
                Assert.assertTrue(pc.getServiceWithMavenArtifacts().get("testService").get(0).getArtifactId().equals("art2"));
                Assert.assertTrue(pc.getServiceWithMavenArtifacts().get("testService").get(0).getGroupId().equals("grp2"));
                Assert.assertTrue(pc.getServiceWithMavenArtifacts().get("testService").get(0).getName().equals("art-grp2"));
                Assert.assertTrue(pc.getServiceWithMavenArtifacts().get("testService").get(0).getVersion().equals("ver"));
            }
            else if(Lists.newArrayList("2","5").contains(pc.getP1().getPatchNumber()) && Lists.newArrayList("2","5").contains(pc.getP2().getPatchNumber())) {
                Assert.assertTrue(pc.getServiceWithMavenArtifacts().isEmpty());
                Assert.assertTrue(pc.getDockerServices().isEmpty());
                Assert.assertTrue(pc.getDbObjects().size() == 2);
                Assert.assertTrue(Lists.newArrayList("module_dbo2","module_dbo2_b").contains(pc.getDbObjects().get(0).getModuleName()));
                Assert.assertTrue(Lists.newArrayList("filename_dbo2","filename_dbo2_b").contains(pc.getDbObjects().get(0).getFileName()));
                Assert.assertTrue(Lists.newArrayList("patch_dbo2","patch_dbo2_b").contains(pc.getDbObjects().get(0).getFilePath()));
                Assert.assertTrue(Lists.newArrayList("module_dbo2","module_dbo2_b").contains(pc.getDbObjects().get(1).getModuleName()));
                Assert.assertTrue(Lists.newArrayList("filename_dbo2","filename_dbo2_b").contains(pc.getDbObjects().get(1).getFileName()));
                Assert.assertTrue(Lists.newArrayList("patch_dbo2","patch_dbo2_b").contains(pc.getDbObjects().get(1).getFilePath()));
            }
            else {
                Assert.fail("Should never be here !!");
            }
        });
    }

    @Test
    public void testNoConflict() {
        Patch p1 = Patch.builder().patchNumber("1").dockerServices(Lists.newArrayList("dockerService")).build();
        List<DbObject> dbo = Lists.newArrayList(DbObject.builder().fileName("f").filePath("p").moduleName("m").build());
        Patch p2 = Patch.builder().patchNumber("2").dbPatch(DBPatch.builder().dbObjects(dbo).build()).build();

        PatchConflictsChecker checker = PatchConflictsCheckerImpl.create();
        checker.addPatch(p1);
        checker.addPatch(p2);

        Assert.assertTrue(checker.checkConflicts().isEmpty());
    }

}
