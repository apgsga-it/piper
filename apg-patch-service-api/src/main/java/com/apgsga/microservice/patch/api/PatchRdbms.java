package com.apgsga.microservice.patch.api;

import java.util.List;

public interface PatchRdbms {

    /**
     * Update a Patch with the status passed as parameter
     * @param patchNumber
     * @param toStatus
     */
    // TODO JHE (18.08.2020) : De we want to wait and return a boolean as confirmation the request has been successfuly exeucted ??
    void executeStateTransitionActionInDb(String patchNumber, String toStatus);

    /**
     *
     * @param statusCode : eg: 80, 25, 15
     * @return : List of Patch number currently in the statusCode passed as parameter
     */
    List<String> patchIdsForStatus(String statusCode);

}
