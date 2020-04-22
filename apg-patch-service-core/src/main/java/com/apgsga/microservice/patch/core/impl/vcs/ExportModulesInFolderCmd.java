package com.apgsga.microservice.patch.core.impl.vcs;

import java.util.List;

public class ExportModulesInFolderCmd extends PatchVcsCommand {

    public ExportModulesInFolderCmd(String prodBranch, String patchBranch, List<String> modules, String additionalOptions) {
        super(patchBranch,prodBranch, additionalOptions, modules);
    }

    @Override
    protected String[] getFristPart() {
        return new String[] {  "-f", "export", "-r", patchTag, additionalOptions};
    }
}
