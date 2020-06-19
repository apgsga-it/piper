package com.apgsga.microservice.patch.core.impl.vcs;

public class RmTmpCheckoutFolder implements VcsCommand {

    private String folderToBeDeleted;

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
