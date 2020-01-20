package com.apgsga.microservice.patch.server.impl.vcs;

import java.util.List;

public class CheckoutModulesInFolderCmd extends PatchVcsCommand {

    public CheckoutModulesInFolderCmd(String cvsBranch, List<String> modules, String lastPart) {
        super(cvsBranch, modules, lastPart);
    }

    @Override
    protected String[] getFristPart() {
        return new String[] {  "-f", "checkout", "-r", patchTag, "-d", lastPart , String.join(" ", modules)};
    }
}
