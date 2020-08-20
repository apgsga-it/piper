package com.apgsga.patch.db.integration.api;

import java.util.List;

public interface PatchRdbms {

    /**
     * Update a Patch with the status passed as parameter
     * @param patchNumber
     * @param statusNum
     */
    void executeStateTransitionActionInDb(String patchNumber, Long statusNum);

    /**
     *
     * @param statusCode : eg: 80, 25, 15
     * @return : List of Patch number currently in the statusCode passed as parameter
     */
    List<String> patchIdsForStatus(String statusCode);

}
