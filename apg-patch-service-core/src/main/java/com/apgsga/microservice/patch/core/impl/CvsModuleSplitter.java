package com.apgsga.microservice.patch.core.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class CvsModuleSplitter {

    private String defaultCvsBranch;
    private String cvsConfigSpecificBranchFilePath;
    private List<String> mavenArtifactsModuleNames;

    private CvsModuleSplitter(){}

    public static CvsModuleSplitter create() {
        return new CvsModuleSplitter();
    }

    public CvsModuleSplitter withDefaultCvsBranch(String defaultCvsBranch) {
        this.defaultCvsBranch = defaultCvsBranch;
        return this;
    }

    public CvsModuleSplitter withCvsConfigSpecificBranchFilePath(String cvsConfigSpecificBranchFilePath) {
        this.cvsConfigSpecificBranchFilePath = cvsConfigSpecificBranchFilePath;
        return this;
    }

    public CvsModuleSplitter withMavenArtifactsModuleNames(List<String> mavenArtifactsModuleNames) {
        this.mavenArtifactsModuleNames = Lists.newArrayList(mavenArtifactsModuleNames);
        return this;
    }

    public Map<String,List<String>> splitModuleForBranch() throws IOException {
        Map<String, List<String>> result = Maps.newHashMap();
        // Specific branch configuration is define within cvsConfigSpecificBranchFilePath, if it exists
        if (Files.exists(new File(cvsConfigSpecificBranchFilePath).toPath())) {
            doSplit(result);
        } else {
            result.put(defaultCvsBranch, mavenArtifactsModuleNames);
        }
        return result;
    }

    private void doSplit(Map<String, List<String>> result) throws IOException {
        Properties branchConfig = new Properties();
        branchConfig.load(new FileInputStream(cvsConfigSpecificBranchFilePath));
        branchConfig.forEach((k,v) -> {
            if(!((String)k).equalsIgnoreCase(defaultCvsBranch)) {
                List<String> modulesForSpecificBranch = Arrays.asList(v.toString().split(","));
                List<String> modulesForCurrentSpecificBranch = mavenArtifactsModuleNames.stream().filter(m -> modulesForSpecificBranch.contains(m)).collect(Collectors.toList());
                if (!modulesForCurrentSpecificBranch.isEmpty()) {
                    result.put((String) k, modulesForCurrentSpecificBranch);
                    mavenArtifactsModuleNames.removeIf(m -> modulesForCurrentSpecificBranch.contains(m));
                }
            }
        });
        if(!mavenArtifactsModuleNames.isEmpty()) {
            result.put(defaultCvsBranch,mavenArtifactsModuleNames);
        }
    }
}
