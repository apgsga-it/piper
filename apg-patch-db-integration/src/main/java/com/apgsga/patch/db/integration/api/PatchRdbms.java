package com.apgsga.patch.db.integration.api;


import com.apgsga.microservice.patch.api.NotificationParameters;

import java.util.List;

public interface PatchRdbms {

    /**
     * notify the DB that a task has been correctly done for a patch
     * @param params with witch are notified
     */
    void notify(NotificationParameters params);

}
