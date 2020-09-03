package com.apgsga.microservice.patch.core.commands;

import com.apgsga.microservice.patch.core.commands.patch.vcs.PatchSshCommand;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;

public abstract class CommandBaseImpl implements Command {

    protected static final Log LOGGER = LogFactory.getLog(PatchSshCommand.class.getName());

    protected boolean noSystemCheck = false;

    @Override
    public String[] getCommand() {
        String[] processBuilderParm;
        if (SystemUtils.IS_OS_WINDOWS && !noSystemCheck) {
            processBuilderParm = new String[] { "bash.exe", "-c", "-s", getParameterSpaceSeperated() };
        } else {
            processBuilderParm = getParameterAsArray();
        }
        LOGGER.info("ProcessBuilder Parameters: " + Arrays.toString(processBuilderParm).toString());
        return processBuilderParm;
    }

    protected abstract String[] getParameterAsArray();

    protected abstract String getParameterSpaceSeperated();

    @Override
    public void noSystemCheck(boolean noCheck) {
        this.noSystemCheck = noCheck;
    }
}
