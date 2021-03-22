package com.apgsga.microservice.patch.core.commands;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LoggingMockProcessBuilderCmdRunnerFactory implements CommandRunnerFactory {

    protected final Log LOGGER = LogFactory.getLog(getClass());


    @Override
    public CommandRunner create() {
        LOGGER.info("Create LoggingProcessBuilderCmdRunnerFactory ");
        return new LoggingMockProcessBuilderCmdRunner();
    }
}
