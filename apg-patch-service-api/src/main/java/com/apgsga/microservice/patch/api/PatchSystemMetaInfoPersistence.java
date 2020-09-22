package com.apgsga.microservice.patch.api;

public interface PatchSystemMetaInfoPersistence {

    // TODO JHE (22.09.2020): The interface is obviously not complete. Will be complete when we'll start developing our Pipelines, and see what's exactly required.

    /**
     *
     * @return : load content of the onDemandTarget.json file
     */
    OnDemandTargets onDemandTargets();

    /**
     *
     * @return : load content of the StageMappings.json file
     */
    StageMappings stageMappings();

    /**
     *
     * @return : load content of the TargetInstances.json file
     */
    TargetInstances targetInstances();


    /**
     *
     * @return : StageMapping for the corresponding toStatus
     * @param toStatus : eg. : Entwicklunginstallationbereit, Anwendertestinstallationbereit
     */
    StageMapping stageMappingFor(String toStatus);

    /**
     *
     * @param toStatus : the current patch status number
     * @return : the next patch status
     */
    Integer findStatus(String toStatus);
}
