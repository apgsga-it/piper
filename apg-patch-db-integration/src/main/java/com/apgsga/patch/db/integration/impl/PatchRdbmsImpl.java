package com.apgsga.patch.db.integration.impl;

import com.apgsga.microservice.patch.api.NotificationParameters;
import com.apgsga.patch.db.integration.api.PatchRdbms;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Types;
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

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withSchemaName("cm")
                .withCatalogName("cm_patch_workflow_f_pa")
                .withProcedureName("notify_action_response")
                .declareParameters(new SqlParameter("p_list_of_patches", Types.VARCHAR),
                        new SqlParameter("p_installation_target", Types.VARCHAR),
                        new SqlParameter("p_status_message", Types.VARCHAR));

        SqlParameterSource in = new MapSqlParameterSource()
                .addValue("p_list_of_patches", params.getPatchNumbers())
                .addValue("p_installation_target", params.getInstallationTarget())
                .addValue("p_status_message", params.getNotification());

        jdbcCall.execute(in);

    }

}
