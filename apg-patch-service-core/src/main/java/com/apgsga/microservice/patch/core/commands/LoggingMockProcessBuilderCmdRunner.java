package com.apgsga.microservice.patch.core.commands;

import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;
import java.util.List;

public class LoggingMockProcessBuilderCmdRunner implements CommandRunner {

    protected final Log LOGGER = LogFactory.getLog(getClass());

    @Override
    public void preProcess() {
        LOGGER.info("pre-processing ...");
    }

    @Override
    public List<String> run(Command cmd) {
        LOGGER.info("Following cmd would be ran : " + Arrays.toString(Arrays.stream(cmd.getCommand()).toArray()));
        return Lists.newArrayList();
    }

    @Override
    public void postProcess() {
        LOGGER.info("post-processing ...");
    }
}
