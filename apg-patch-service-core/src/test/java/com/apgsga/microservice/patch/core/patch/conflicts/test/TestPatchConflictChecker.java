package com.apgsga.microservice.patch.core.patch.conflicts.test;

import com.apgsga.microservice.patch.api.*;
import com.apgsga.microservice.patch.core.patch.conflicts.PatchConflict;
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
        PatchConflictsChecker checker = PatchConflictsChecker.create();
        checker.addPatch(p1);
        checker.addPatch(p2);
        List<PatchConflict> patchConflicts = checker.checkConflicts();
        Assert.assertTrue(patchConflicts != null);
        Assert.assertTrue(!patchConflicts.isEmpty());
        Assert.assertTrue(patchConflicts.size() == 1);
        Assert.assertTrue(patchConflicts.get(0).getDockerServices().size() == 1);
        Assert.assertTrue(patchConflicts.get(0).getDockerServices().get(0).equals("dockerService"));
        Assert.assertTrue(patchConflicts.get(0).getDbObjects().isEmpty());
        Assert.assertTrue(patchConflicts.get(0).getMavenArtifacts().isEmpty());
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
        PatchConflictsChecker checker = PatchConflictsChecker.create();
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
                Assert.assertTrue(pc.getMavenArtifacts().isEmpty());
                Assert.assertTrue(pc.getDbObjects().isEmpty());
            }
            else {
                Assert.assertTrue(Lists.newArrayList(p2.getPatchNumber(),p3.getPatchNumber()).contains(pc.getP1().getPatchNumber()));
                Assert.assertTrue(Lists.newArrayList(p2.getPatchNumber(),p3.getPatchNumber()).contains(pc.getP2().getPatchNumber()));
                Assert.assertTrue(pc.getDockerServices().size() == 1);
                Assert.assertTrue(pc.getDockerServices().get(0).equals("dockerService_2"));
                Assert.assertTrue(pc.getMavenArtifacts().isEmpty());
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

        PatchConflictsChecker conflictsChecker = PatchConflictsChecker.create();
        conflictsChecker.addPatch(p1);
        conflictsChecker.addPatch(p2);

        List<PatchConflict> patchConflicts = conflictsChecker.checkConflicts();
        Assert.assertTrue(patchConflicts.size() == 1);
        Assert.assertTrue(patchConflicts.get(0).getDockerServices().isEmpty());
        Assert.assertTrue(patchConflicts.get(0).getMavenArtifacts().isEmpty());
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

        PatchConflictsChecker conflictsChecker = PatchConflictsChecker.create();
        conflictsChecker.addPatch(p1);
        conflictsChecker.addPatch(p2);
        conflictsChecker.addPatch(p3);
        conflictsChecker.addPatch(p4);

        List<PatchConflict> patchConflicts = conflictsChecker.checkConflicts();
        Assert.assertTrue(patchConflicts.size() == 1);
        Assert.assertTrue(patchConflicts.get(0).getDockerServices().isEmpty());
        Assert.assertTrue(patchConflicts.get(0).getMavenArtifacts().isEmpty());
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

        PatchConflictsChecker conflictsChecker = PatchConflictsChecker.create();
        conflictsChecker.addPatch(p1);
        conflictsChecker.addPatch(p2);

        List<PatchConflict> patchConflicts = conflictsChecker.checkConflicts();
        Assert.assertTrue(patchConflicts.size() == 1);
        Assert.assertTrue(patchConflicts.get(0).getDbObjects().isEmpty());
        Assert.assertTrue(patchConflicts.get(0).getDockerServices().isEmpty());
        Assert.assertTrue(patchConflicts.get(0).getMavenArtifacts().size() == 1);
        Assert.assertTrue(patchConflicts.get(0).getMavenArtifacts().get(0).getArtifactId().equals("art"));
        Assert.assertTrue(patchConflicts.get(0).getMavenArtifacts().get(0).getGroupId().equals("grp"));
        Assert.assertTrue(patchConflicts.get(0).getMavenArtifacts().get(0).getName().equals("art.grp"));
        Assert.assertTrue(patchConflicts.get(0).getMavenArtifacts().get(0).getVersion().equals("ver"));
        Assert.assertTrue(Lists.newArrayList(p1.getPatchNumber(),p2.getPatchNumber()).contains(patchConflicts.get(0).getP1().getPatchNumber()));
        Assert.assertTrue(Lists.newArrayList(p1.getPatchNumber(),p2.getPatchNumber()).contains(patchConflicts.get(0).getP2().getPatchNumber()));
    }

    here, write next test for mavenARtifacts

}
