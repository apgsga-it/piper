package com.apgsga.patch.db.integration.impl;

import com.apgsga.microservice.patch.api.NotificationParameters;
import com.apgsga.patch.db.integration.api.PatchRdbms;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Component("patchRdbms")
@Profile("patchOMat")
public class PatchRdbmsImpl implements PatchRdbms {

    protected static final Log LOGGER = LogFactory.getLog(PatchRdbmsImpl.class.getName());

    @Autowired
    DataSource dataSource;

    @Override
    public void notify(NotificationParameters params) {
        LOGGER.info("Notifying DB for : " + params.toString());


        // JHE (02.03.21) : Quick and dirty just ot be sure DB call is working ...
        //                  Will be correctly implemented as soon as correct store proc on DB will be ready (cm_patch_workflow_f_pa.notify_action_response)

        String sql = "{call cm.cm_utility_pa.testJhe(:p_param1,:p_param2)}";

        MapSqlParameterSource sqlParamMap = new MapSqlParameterSource();
        sqlParamMap.addValue("p_param1", "error = " + params.getErrorNotification());
        sqlParamMap.addValue("p_param2", "success = " + params.getSuccessNotification());

        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource);
        template.execute(sql, sqlParamMap, new PreparedStatementCallback<Object>() {
                    @Override
                    public Boolean doInPreparedStatement(PreparedStatement ps)
                            throws SQLException, DataAccessException {
                        return ps.execute();
                    }
        });
    }

    @Override
    @Deprecated // TODO JHE (18.11.2020): Not 100% sure yet, but this will most probably be removed
    public List<String> patchIdsForStatus(String statusCode) {
        /*
        String sql = "SELECT id FROM cm_patch_f p INNER JOIN cm_patch_status_f s ON p.status = s.pat_status WHERE s.pat_status = " + statusCode;
        return jdbcTemplate.queryForList(sql, String.class);
        */
        return null;
    }
}
