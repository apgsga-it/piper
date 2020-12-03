package com.apgsga.microservice.patch.api;

import java.util.List;

public interface PatchSystemMetaInfoPersistence {

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
     * @param service
     * @return : the packager name define in our ServiceMetadata.json file for the corresponding service
     */
    List<Package> packagesFor(Service service);

    /**
     *
     * @param stageName : example: Informatiktest
     * @return ex. CHTI211
     */
    String targetFor(String stageName);

}
