package com.apgsga.microservice.patch.core.impl.jenkins;


import com.google.common.collect.Sets;

import java.util.Set;

public class InstallDbObjectsInfos {

    public String dbPatchTag;
    public String dbPatchBranch;
    public Set<String> dbObjectsModuleNames = Sets.newHashSet();

    public InstallDbObjectsInfos(String dbPatchTag, String dbPatchBranch) {
        this.dbPatchTag = dbPatchTag;
        this.dbPatchBranch = dbPatchBranch;
    }
}
