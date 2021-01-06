package com.apgsga.patch.db.integration.impl;

import com.apgsga.microservice.patch.api.NotificationParameters;
import com.apgsga.patch.db.integration.api.PatchRdbms;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component("patchRdbms")
@Profile("patchOMat")
public class PatchRdbmsImpl implements PatchRdbms {

    protected static final Log LOGGER = LogFactory.getLog(PatchRdbmsImpl.class.getName());

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public void notify(NotificationParameters params) {
        LOGGER.info("Notifying DB for : " + params.toString());
        SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate).withProcedureName(NotificationParameters.PATCH_NOTIFICATION_PROCEDURE_NAME);
        SqlParameterSource in = new MapSqlParameterSource(params.getAllParameters());
        Map<String, Object> simpleJdbcCallResult = simpleJdbcCall.execute(in);
        LOGGER.info("Notify DB Result = " + simpleJdbcCallResult);

    }

    @Override
    @Deprecated // TODO JHE (18.11.2020): Not 100% sure yet, but this will most probably be removed
    public List<String> patchIdsForStatus(String statusCode) {
        String sql = "SELECT id FROM cm_patch_f p INNER JOIN cm_patch_status_f s ON p.status = s.pat_status WHERE s.pat_status = " + statusCode;
        return jdbcTemplate.queryForList(sql, String.class);
    }
}
