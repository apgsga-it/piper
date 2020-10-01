package com.apgsga.microservice.patch.core.commands.jenkins.ssh;

import com.apgsga.microservice.patch.core.commands.CommandBaseImpl;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

public abstract class JenkinsSshCommand extends CommandBaseImpl {

    protected static final Log LOGGER = LogFactory.getLog(JenkinsSshCommand.class.getName());

    protected String jenkinsHost;

    protected String jenkinsSshUser;

    protected String jenkinsSshPort;

    public JenkinsSshCommand(String jenkinsHost, String jenkinsSshPort, String jenkinsSshUser) {
        super();
        this.jenkinsHost = jenkinsHost;
        this.jenkinsSshPort = jenkinsSshPort;
        this.jenkinsSshUser = jenkinsSshUser;
    }

    public static JenkinsSshCommand createJenkinsSshBuildJobAndReturnImmediatelyCmd(String jenkinsHost, String jenkinsSshPort, String jenkinsSshUser, String jobName) {
        return new JenkinsSshBuildJobCmd(jenkinsHost,jenkinsSshPort,jenkinsSshUser,jobName,false,false);
    }

    public static JenkinsSshCommand createJenkinsSshBuildJobAndReturnImmediatelyCmd(String jenkinsHost, String jenkinsSshPort, String jenkinsSshUser, String jobName, Map<String,String> jobParameters, Map<String,String> fileParams) {
        return new JenkinsSshBuildJobCmd(jenkinsHost,jenkinsSshPort,jenkinsSshUser,jobName, jobParameters,fileParams,false,false);
    }

    public static JenkinsSshCommand createJenkinsSshBuildJobAndWaitForStartCmd(String jenkinsHost, String jenkinsSshPort, String jenkinsSshUser, String jobName, Map<String,String> jobParameters,Map<String,String> fileParams) {
        return new JenkinsSshBuildJobCmd(jenkinsHost,jenkinsSshPort,jenkinsSshUser,jobName,jobParameters,fileParams,true,false);
    }

    public static JenkinsSshCommand createJenkinsSshBuildJobAndWaitForStartCmd(String jenkinsHost, String jenkinsSshPort, String jenkinsSshUser, String jobName) {
        return new JenkinsSshBuildJobCmd(jenkinsHost,jenkinsSshPort,jenkinsSshUser,jobName,true,false);
    }

    public static JenkinsSshCommand createJenkinsSshBuildJobAndWaitForCompleteCmd(String jenkinsHost, String jenkinsSshPort, String jenkinsSshUser, String jobName, Map<String,String> jobParameters) {
        return new JenkinsSshBuildJobCmd(jenkinsHost,jenkinsSshPort,jenkinsSshUser,jobName,jobParameters,null,false,true);
    }

    public static JenkinsSshCommand createJenkinsSshBuildJobAndWaitForCompleteCmd(String jenkinsHost, String jenkinsSshPort, String jenkinsSshUser, String jobName) {
        return new JenkinsSshBuildJobCmd(jenkinsHost,jenkinsSshPort,jenkinsSshUser,jobName,false,true);
    }

    public static JenkinsSshCommand createJenkinsSshStopBuildCmd(String jenkinsHost, String jenkinsSshPort, String jenkinsSshUser, String jobName) {
        return new JenkinsSshStopBuildCmd(jenkinsHost,jenkinsSshPort,jenkinsSshUser,jobName,false,false);
    }

    @Override
    public String[] getCommand() {
        if(hasFileParam() && SystemUtils.IS_OS_WINDOWS) {
            throw ExceptionFactory.createPatchServiceRuntimeException(
                    "JenkinsSshCommand.startJobWithFileParam.windows.error", new Object[] {});
        }
        String[] processBuilderParm;
        if (SystemUtils.IS_OS_WINDOWS && !noSystemCheck) {
            processBuilderParm = new String[] { "bash.exe", "-c", "-s", " " + getParameterSpaceSeperated() };
        } else {
            processBuilderParm = getParameterAsArray();
        }
        LOGGER.info("ProcessBuilder Parameters: " + Arrays.toString(processBuilderParm).toString());
        return processBuilderParm;
    }

    @Override
    protected String[] getParameterAsArray() {
        String[] parameter;
        if(hasFileParam()) {
            // JHE (01.10.2020): in that case we do not get the first part from the common place because the command has to be built in "one shot".
            //                   see also : https://stackoverflow.com/questions/3776195/using-java-processbuilder-to-execute-a-piped-command
            //                          -> we're getting the same behavior, but with the "cat" command
            parameter = Arrays.stream(getJenkinsCmd()).toArray(String[]::new);
        }
        else {
            parameter = Stream.concat(Arrays.stream(getFirstPart()), Arrays.stream(getJenkinsCmd())).toArray(String[]::new);
        }
        return parameter;
    }

    @Override
    protected String getParameterSpaceSeperated() {
        String[] processParm = Stream.concat(Arrays.stream(getFirstPart()), Arrays.stream(getJenkinsCmd()))
                .toArray(String[]::new);
        String parameter = String.join(" ", processParm);
        return parameter;
    }

    private String[] getFirstPart() {
         return new String[]{"ssh", "-l", jenkinsSshUser, "-p", jenkinsSshPort, jenkinsHost};
    }

    protected abstract boolean hasFileParam();

    // TODO JHE (01.10.2020) : Really need this as abstract here ?
    protected abstract String getFileParameterName();

    protected abstract String[] getJenkinsCmd();

    @Override
    public void noSystemCheck(boolean noCheck) {
        super.noSystemCheck(noCheck);
    }
}
