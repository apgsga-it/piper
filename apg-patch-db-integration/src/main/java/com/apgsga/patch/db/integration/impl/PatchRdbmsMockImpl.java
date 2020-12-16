package com.apgsga.patch.db.integration.impl;

import com.apgsga.microservice.patch.api.NotificationParameters;
import com.apgsga.patch.db.integration.api.PatchRdbms;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Profile("rdbmsMock")
@Component("patchRdbms")
public class PatchRdbmsMockImpl implements PatchRdbms {

    protected static final Log LOGGER = LogFactory.getLog(PatchRdbmsMockImpl.class.getName());

    @Override
    public void notify(NotificationParameters params) {
        LOGGER.warn("Mocked notify with NotificationParameters: " + params.toString());
    }

    @Override
    @Deprecated // TODO JHE (18.11.2020): Not 100% sure yet, but this will most probably be removed
    public List<String> patchIdsForStatus(String statusCode) {
        LOGGER.warn("Mocked patchIdsForStatus with statusCode: " + statusCode);
        return Collections.emptyList();
    }
}
