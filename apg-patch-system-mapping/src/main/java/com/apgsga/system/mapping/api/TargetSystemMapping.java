package com.apgsga.system.mapping.api;

import java.util.List;
import java.util.Map;

public interface TargetSystemMapping {

    /**
     *
     * @param toStatus : the current patch status number
     * @return : the next patch status
     */
    String findStatus(String toStatus);

    /**
     *
     * @param serviceName
     * @param target
     * @return : eg.: oracle-db, linuxservice, linuxbasedwindowsfilesystem
     */
    String serviceTypeFor(String serviceName, String target);

    /**
     *
     * @param serviceName
     * @param target
     * @return : the target on which the service will be deployed and installed
     */
    String installTargetFor(String serviceName, String target);

    /**
     *
     * @param target
     * @return : if the service will be installed on a Light instance
     */
    boolean isLightInstance(String target);

    /**
     *
     * @return : list of valid states into which Piper can set a Patch to
     */
    List<String> validToStates();

    /**
     *
     * @return : list of targets on which an onDemand installation can be started
     */
    List<String> listInstallTargets();

    /**
     *
     * @return : Map with information about the stageMappings section
     */
    Map<String,Map<String,String>> stateMap();
}
