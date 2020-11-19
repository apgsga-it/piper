package com.apgsga.patch.db.integration.api;

import com.apgsga.patch.db.integration.impl.NotifyDbParameters;

import java.util.List;

public interface PatchRdbms {

    /**
     * notify the DB that a task has been correctly done for a patch
     * @param params
     */
    void notifyDb(NotifyDbParameters params);

    /**
     *
     * @param statusCode : eg: 80, 25, 15
     * @return : List of Patch number currently in the statusCode passed as parameter
     */
    List<String> patchIdsForStatus(String statusCode);

}
