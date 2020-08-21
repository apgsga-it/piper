package com.apgsga.patch.db.integration.impl;

import com.apgsga.patch.db.integration.api.PatchRdbms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("patchRdbms")
public class PatchRdbmsImpl implements PatchRdbms {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public void executeStateTransitionActionInDb(String patchNumber, Long statusNum) {
        Long id = Long.valueOf(patchNumber);
        String sql = "update cm_patch_f set status = " + statusNum + " where id = " + id;
        int nbRowsUpdated = jdbcTemplate.update(sql);
    }

    @Override
    public List<String> patchIdsForStatus(String statusCode) {
        String sql = "SELECT id FROM cm_patch_f p INNER JOIN cm_patch_status_f s ON p.status = s.pat_status WHERE s.pat_status = " + statusCode ;
        List<String> rows = jdbcTemplate.queryForList(sql,String.class);
        return rows;
    }
}
