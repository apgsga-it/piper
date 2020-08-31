package com.apgsga.microservice.patch.core.ssh;

import com.apgsga.microservice.patch.core.ssh.patch.vcs.PatchSshCommand;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;

//TODO JHE (27.08.2020) : to be renamed without Ssh
public abstract class SshCommandBaseImpl implements SshCommand {

    protected static final Log LOGGER = LogFactory.getLog(PatchSshCommand.class.getName());

    protected boolean noSystemCheck = false;

    // TODO JHE: Probably this method has to be abstract
    @Override
    public String[] getCommand() {
        String[] processBuilderParm;
        if (SystemUtils.IS_OS_WINDOWS && !noSystemCheck) {
//            processBuilderParm = new String[] { "bash.exe", "-c", "-s", "cvs " + getParameterSpaceSeperated() };
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
