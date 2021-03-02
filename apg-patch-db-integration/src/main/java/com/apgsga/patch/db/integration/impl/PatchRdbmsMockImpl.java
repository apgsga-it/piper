package com.apgsga.patch.db.integration.impl;

import com.apgsga.microservice.patch.api.NotificationParameters;
import com.apgsga.patch.db.integration.api.PatchRdbms;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Profile("patchOMock")
@Component("patchRdbms")
public class PatchRdbmsMockImpl implements PatchRdbms {

    protected static final Log LOGGER = LogFactory.getLog(PatchRdbmsMockImpl.class.getName());

    @Override
    public void notify(NotificationParameters params) {
        LOGGER.warn("Mocked notify with NotificationParameters: " + params.toString());
    }

    @Override
    public List<String> patchIdsForStatus(String statusCode) {
        return null;
    }
}
