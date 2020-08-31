package com.apgsga.microservice.patch.core.commands.patch.vcs;

import java.util.List;

public class ExportModulesInFolderCmd extends PatchSshCommand {

    public ExportModulesInFolderCmd(String prodBranch, String patchBranch, List<String> modules, String additionalOptions) {
        super(patchBranch,prodBranch, additionalOptions, modules);
    }

    @Override
    protected String[] getFirstPart() {
        return new String[] {  "-f", "export", "-r", patchTag, additionalOptions};
    }
}
