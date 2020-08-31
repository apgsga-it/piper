package com.apgsga.microservice.patch.core.ssh.jenkins;

import com.apgsga.microservice.patch.core.ssh.SshCommandBaseImpl;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

public abstract class JenkinsSshCommand extends SshCommandBaseImpl {

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

    public static JenkinsSshCommand createJenkinsSshBuildJobAndReturnImmediatelyCmd(String jenkinsHost, String jenkinsSshPort, String jenkinsSshUser, String jobName, Map<String,String> jobParameters) {
        return new JenkinsSshBuildJobCmd(jenkinsHost,jenkinsSshPort,jenkinsSshUser,jobName, jobParameters,null,false,false);
    }

    public static JenkinsSshCommand createJenkinsSshBuildJobAndWaitForStartCmd(String jenkinsHost, String jenkinsSshPort, String jenkinsSshUser, String jobName, Map<String,String> jobParameters,Map<String,File> fileParams) {
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

    // TODO JHE: Consider moving this to a super class
    @Override
    public String[] getCommand() {
        String[] processBuilderParm;
        if (SystemUtils.IS_OS_WINDOWS && !noSystemCheck) {
            processBuilderParm = new String[] { "bash.exe", "-c", "-s", "ssh " +  getParameterSpaceSeperated() };
        } else {
            processBuilderParm = getParameterAsArray();
        }
        LOGGER.info("ProcessBuilder Parameters: " + Arrays.toString(processBuilderParm).toString());
        return processBuilderParm;
    }

    @Override
    protected String[] getParameterAsArray() {
        String[] parameter = Stream.concat(Arrays.stream(getFirstPart()), Arrays.stream(getJenkinsCmd()))
                .toArray(String[]::new);

        // TODO JHE (24.08.2020 ): Really needed ??
        /*
        if(lastPart != null) {
            parameter = Stream.concat(Arrays.stream(parameter), Arrays.stream(new String[] {lastPart})).toArray(String[]::new);
        }
        */

        /*
        // TODO (che, 4.4.2018) : either via bash or path
        // TODO (che, 4.4.2018) : cvs Root either in Enviroment or Configuration
        String[] processBuilderParm = Stream.concat(Arrays.stream(new String[] { "/usr/bin/cvs", "-d", "/var/local/cvs/root" }), Arrays.stream(parameter)).toArray(String[]::new);
        return processBuilderParm;

         */

//        return Arrays.stream(parameter).toArray(String[]::new);

        return parameter;
    }

    @Override
    protected String getParameterSpaceSeperated() {
        String[] processParm = Stream.concat(Arrays.stream(getFirstPart()), Arrays.stream(getJenkinsCmd()))
                .toArray(String[]::new);

        // TODO JHE (24.08.2020 ): Really needed ??
        /*
        if(lastPart != null) {
            processParm = Stream.concat(Arrays.stream(processParm), Arrays.stream(new String[] {lastPart})).toArray(String[]::new);
        }

         */
        String parameter = String.join(" ", processParm);
        // TODO JHE (24.08.2020 ): Really needed ??
        /*
        if (additionalOptions != null) {
            return additionalOptions + " " + parameter;
        }
         */
        return parameter;
    }

    private String[] getFirstPart() {
        return new String[] {  "-l", jenkinsSshUser, "-p", jenkinsSshPort, jenkinsHost};
    }

    protected abstract String[] getJenkinsCmd();

    @Override
    public void noSystemCheck(boolean noCheck) {
        super.noSystemCheck(noCheck);
    }
}
