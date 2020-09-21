package com.apgsga.microservice.patch.api;

public interface PatchSystemMetaInfoPersistence {

    // TODO JHE: implements what's missing based on tests .... all the rest, what we're not sure yet if needed ot not, won't be implemented

    /**
     *
     * @return : load content of the onDemandTarget.json file
     */
    OnDemandTargets onDemandTargets();

    /**
     *
     * @return : load content of the StageMappings.json file
     */
    StageMappings stageMapping();

    /**
     *
     * @return : load content of the TargetInstances.json file
     */
    TargetInstances targetInstances();

}
