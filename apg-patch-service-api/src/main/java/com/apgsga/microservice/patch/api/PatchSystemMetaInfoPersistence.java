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
     * @return: load content of the ServiceMetadata.json file
     */
    ServicesMetaData servicesMetaData();


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

    /**
     *
     * @param service
     * @return : the packager name define in our ServiceMetadata.json file for the corresponding service
     */
    String packagerNameFor(Service service);
}
