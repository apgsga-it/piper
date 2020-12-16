package com.apgsga.microservice.patch.core.commands.patch.vcs;

import com.apgsga.microservice.patch.core.commands.Command;

public class RmTmpCheckoutFolder implements Command {

    private final String folderToBeDeleted;

    public RmTmpCheckoutFolder(String coFolder) {
        this.folderToBeDeleted = coFolder;
    }

    @Override
    public String[] getCommand() {
        return new String[] {"/bin/rm", "-Rf", folderToBeDeleted};
    }

    @Override
    public void noSystemCheck(boolean noChecnk) {
        // Leave empty on purpose ... for now we don't need to do anything with it.
    }
}
