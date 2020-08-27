package com.apgsga.microservice.patch.core.impl.vcs;

import java.util.List;

public class SimpleCommand implements VcsCommand{

    private final List<String> processBuilderArgs;

    public SimpleCommand(List<String> processBuilderArgs) {
        this.processBuilderArgs = processBuilderArgs;
    }

    @Override
    public String[] getCommand() {
        return processBuilderArgs.toArray(new String[0]);
    }

    @Override
    public void noSystemCheck(boolean noChecnk) {

    }
}
