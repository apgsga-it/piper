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

        String sql = "{call cm.cm_patch_workflow_f_pa.notify_action_response(:p_list_of_patches,:p_installation_target,:p_status_message)}";

        MapSqlParameterSource sqlParamMap = new MapSqlParameterSource();
        sqlParamMap.addValue("p_list_of_patches", params.getPatchNumbers());
        sqlParamMap.addValue("p_installation_target", params.getInstallationTarget());
        sqlParamMap.addValue("p_status_message", params.getNotification());

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
