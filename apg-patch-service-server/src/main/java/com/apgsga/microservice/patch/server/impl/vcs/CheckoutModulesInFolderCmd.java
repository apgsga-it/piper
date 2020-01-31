package com.apgsga.microservice.patch.server.impl.vcs;

import java.util.List;

public class CheckoutModulesInFolderCmd extends PatchVcsCommand {

    public CheckoutModulesInFolderCmd(String prodBranch, String patchBranch, List<String> modules, String additionalOptions) {
        super(patchBranch,prodBranch, additionalOptions, modules);
    }

    @Override
    protected String[] getFristPart() {
        // TODO JHE: Test if a export instead of checkout would work, might have better performance
        return new String[] {  "-f", "checkout", "-r", patchTag, additionalOptions};
    }
}
