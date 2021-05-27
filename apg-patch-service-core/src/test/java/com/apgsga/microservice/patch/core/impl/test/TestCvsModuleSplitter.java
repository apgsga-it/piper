package com.apgsga.microservice.patch.core.impl.test;

import com.apgsga.microservice.patch.core.impl.CvsModuleSplitter;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TestCvsModuleSplitter {

    @Test
    public void testWhenConfigurationFileNotExist() throws IOException {
        String defaultCvsBranch = "cvsDefaultBranch";
        List<String> cvsModules = Lists.newArrayList("module.1","module.2","module.3");
        String fileName = "thisIsAWrongName.properties";
        Map<String, List<String>> result = CvsModuleSplitter.create()
                .withCvsConfigSpecificBranchFilePath(fileName)
                .withDefaultCvsBranch(defaultCvsBranch)
                .withMavenArtifactsModuleNames(cvsModules)
                .splitModuleForBranch();
        Assert.assertEquals("wrong number of Branch has been set",1,result.keySet().size());
        Assert.assertEquals("wrong key has been set",defaultCvsBranch,result.keySet().toArray()[0]);
        Assert.assertEquals("missing artifacts for " + defaultCvsBranch + " branch",3,result.get(defaultCvsBranch).size());
        result.get(defaultCvsBranch).forEach(m -> {
            Assert.assertTrue(m + " should be in the list", cvsModules.contains(m));
        });
    }

    @Test
    public void testWhenNoArtifactFoundInConfiguration() throws IOException {
        String defaultCvsBranch = "cvsDefaultBranch";
        List<String> cvsModules = Lists.newArrayList("module.1","module.2","module.3");
        String fileName = "src/test/resources/testCvsConfigSpecificBranchForModules.properties";
        Map<String, List<String>> result = CvsModuleSplitter.create()
                .withCvsConfigSpecificBranchFilePath(fileName)
                .withDefaultCvsBranch(defaultCvsBranch)
                .withMavenArtifactsModuleNames(cvsModules)
                .splitModuleForBranch();
        Assert.assertEquals("wrong number of Branch has been set",1,result.keySet().size());
        Assert.assertEquals("wrong key has been set",defaultCvsBranch,result.keySet().toArray()[0]);
        Assert.assertEquals("missing artifacts for " + defaultCvsBranch + " branch",3,result.get(defaultCvsBranch).size());
        result.get(defaultCvsBranch).forEach(m -> {
            Assert.assertTrue(m + " should be in the list", cvsModules.contains(m));
        });
    }

    @Test
    public void testWhenPartOfArtifactsFoundInConfigurationForOneSpecificBranch() throws IOException {
        String defaultCvsBranch = "cvsDefaultBranch";
        String specificCvsBranch = "specificCvsBranch_1";
        List<String> cvsModulesOnSpecificBranch = Lists.newArrayList("module.1.1","module.1.2");
        List<String> cvsModules = Lists.newArrayList("module.3");
        cvsModules.addAll(cvsModulesOnSpecificBranch);
        String fileName = "src/test/resources/testCvsConfigSpecificBranchForModules.properties";
        Map<String, List<String>> result = CvsModuleSplitter.create()
                .withCvsConfigSpecificBranchFilePath(fileName)
                .withDefaultCvsBranch(defaultCvsBranch)
                .withMavenArtifactsModuleNames(cvsModules)
                .splitModuleForBranch();
        Assert.assertEquals("wrong number of Branch has been set",2,result.keySet().size());
        Assert.assertTrue(defaultCvsBranch + " should be define as a key",result.keySet().contains(defaultCvsBranch));
        Assert.assertTrue(specificCvsBranch + " should be define as a key",result.keySet().contains(specificCvsBranch));
        Assert.assertEquals("missing artifacts for " + defaultCvsBranch + " branch",1,result.get(defaultCvsBranch).size());
        Assert.assertEquals("missing artifacts for " + specificCvsBranch + " branch",2,result.get(specificCvsBranch).size());
        result.get(defaultCvsBranch).forEach(m -> {
            Assert.assertTrue(m + " should be in the list for " + defaultCvsBranch, cvsModules.contains(m));
        });
        result.get(specificCvsBranch).forEach(m -> {
            Assert.assertTrue(m + " should be in the list for " + specificCvsBranch, cvsModulesOnSpecificBranch.contains(m));
        });
    }

    @Test
    public void testWhenPartOfArtifactsFoundInConfigurationForMultipleSpecificBranch() throws IOException {
        String defaultCvsBranch = "cvsDefaultBranch";
        String specificCvsBranch = "specificCvsBranch_1";
        String specificCvsBranch_2 = "specificCvsBranch_2";
        List<String> cvsModulesOnSpecificBranch = Lists.newArrayList("module.1.1","module.1.2");
        List<String> cvsModulesOnSpecificBranch_2 = Lists.newArrayList("module.2.4");
        List<String> cvsModules = Lists.newArrayList("module.3");
        cvsModules.addAll(cvsModulesOnSpecificBranch);
        cvsModules.addAll(cvsModulesOnSpecificBranch_2);
        String fileName = "src/test/resources/testCvsConfigSpecificBranchForModules.properties";
        Map<String, List<String>> result = CvsModuleSplitter.create()
                .withCvsConfigSpecificBranchFilePath(fileName)
                .withDefaultCvsBranch(defaultCvsBranch)
                .withMavenArtifactsModuleNames(cvsModules)
                .splitModuleForBranch();
        Assert.assertEquals("wrong number of Branch has been set",3,result.keySet().size());
        Assert.assertTrue(defaultCvsBranch + " should be define as a key",result.keySet().contains(defaultCvsBranch));
        Assert.assertTrue(specificCvsBranch + " should be define as a key",result.keySet().contains(specificCvsBranch));
        Assert.assertTrue(specificCvsBranch_2 + " should be define as a key",result.keySet().contains(specificCvsBranch_2));
        Assert.assertEquals("missing artifacts for " + defaultCvsBranch + " branch",1,result.get(defaultCvsBranch).size());
        Assert.assertEquals("missing artifacts for " + specificCvsBranch + " branch",2,result.get(specificCvsBranch).size());
        Assert.assertEquals("missing artifacts for " + specificCvsBranch_2 + " branch",1,result.get(specificCvsBranch_2).size());
        result.get(defaultCvsBranch).forEach(m -> {
            Assert.assertTrue(m + " should be in the list for " + defaultCvsBranch, cvsModules.contains(m));
        });
        result.get(specificCvsBranch).forEach(m -> {
            Assert.assertTrue(m + " should be in the list for " + specificCvsBranch, cvsModulesOnSpecificBranch.contains(m));
        });
        result.get(specificCvsBranch_2).forEach(m -> {
            Assert.assertTrue(m + " should be in the list for " + specificCvsBranch_2, cvsModulesOnSpecificBranch_2.contains(m));
        });
    }

    @Test
    public void testWhenAllArtifactsFoundInConfigurationForOneSpecificBranch() throws IOException {
        String defaultCvsBranch = "cvsDefaultBranch";
        String specificCvsBranch = "specificCvsBranch_1";
        List<String> cvsModulesOnSpecificBranch = Lists.newArrayList("module.1.1","module.1.2","module.1.3");
        List<String> cvsModules = Lists.newArrayList();
        cvsModules.addAll(cvsModulesOnSpecificBranch);
        String fileName = "src/test/resources/testCvsConfigSpecificBranchForModules.properties";
        Map<String, List<String>> result = CvsModuleSplitter.create()
                .withCvsConfigSpecificBranchFilePath(fileName)
                .withDefaultCvsBranch(defaultCvsBranch)
                .withMavenArtifactsModuleNames(cvsModules)
                .splitModuleForBranch();
        Assert.assertEquals("wrong number of Branch has been set",1,result.keySet().size());
        Assert.assertTrue(specificCvsBranch + " should be define as a key",result.keySet().contains(specificCvsBranch));
        Assert.assertEquals("missing artifacts for " + specificCvsBranch + " branch",3,result.get(specificCvsBranch).size());
        result.get(specificCvsBranch).forEach(m -> {
            Assert.assertTrue(m + " should be in the list for " + specificCvsBranch, cvsModulesOnSpecificBranch.contains(m));
        });
    }

    @Test
    public void testWhenAllArtifactsFoundInConfigurationForMultipleSpecificBranch() throws IOException {
        String defaultCvsBranch = "cvsDefaultBranch";
        String specificCvsBranch = "specificCvsBranch_1";
        String specificCvsBranch_2 = "specificCvsBranch_2";
        List<String> cvsModulesOnSpecificBranch = Lists.newArrayList("module.1.1","module.1.2","module.1.3");
        List<String> cvsModulesOnSpecificBranch_2 = Lists.newArrayList("module.2.1","module.2.2","module.2.3","module.2.4");
        List<String> cvsModules = Lists.newArrayList();
        cvsModules.addAll(cvsModulesOnSpecificBranch);
        cvsModules.addAll(cvsModulesOnSpecificBranch_2);
        String fileName = "src/test/resources/testCvsConfigSpecificBranchForModules.properties";
        Map<String, List<String>> result = CvsModuleSplitter.create()
                .withCvsConfigSpecificBranchFilePath(fileName)
                .withDefaultCvsBranch(defaultCvsBranch)
                .withMavenArtifactsModuleNames(cvsModules)
                .splitModuleForBranch();
        Assert.assertEquals("wrong number of Branch has been set",2,result.keySet().size());
        Assert.assertTrue(specificCvsBranch + " should be define as a key",result.keySet().contains(specificCvsBranch));
        Assert.assertTrue(specificCvsBranch_2 + " should be define as a key",result.keySet().contains(specificCvsBranch_2));
        Assert.assertEquals("missing artifacts for " + specificCvsBranch + " branch",3,result.get(specificCvsBranch).size());
        Assert.assertEquals("missing artifacts for " + specificCvsBranch_2 + " branch",4,result.get(specificCvsBranch_2).size());
        result.get(specificCvsBranch).forEach(m -> {
            Assert.assertTrue(m + " should be in the list for " + specificCvsBranch, cvsModulesOnSpecificBranch.contains(m));
        });
        result.get(specificCvsBranch_2).forEach(m -> {
            Assert.assertTrue(m + " should be in the list for " + specificCvsBranch_2, cvsModulesOnSpecificBranch_2.contains(m));
        });
    }

}
