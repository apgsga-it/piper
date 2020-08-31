package com.apgsga.microservice.patch.core.ssh.patch.vcs;

import com.apgsga.microservice.patch.core.ssh.SshCommand;

public class RmTmpCheckoutFolder implements SshCommand {

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
