package com.apgsga.patch.db.integration.impl;

import com.apgsga.patch.db.integration.api.PatchRdbms;
import com.apgsga.system.mapping.api.TargetSystemMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("patchRdbms")
public class PatchRdbmsImpl implements PatchRdbms {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("targetSystemMapping")
    TargetSystemMapping tsm;

    @Override
    public void executeStateTransitionActionInDb(String patchNumber, String toStatus) {
        // TODO JHE (18.08.2020) : better map parameter
        Long statusNum = Long.valueOf(tsm.findStatus(toStatus));
        Long id = Long.valueOf(patchNumber);
        String sql = "update cm_patch_f set status = " + statusNum + " where id = " + id;
        int nbRowsUpdated = jdbcTemplate.update(sql);
        System.out.println(nbRowsUpdated + " have been updated!");
    }

    @Override
    public List<String> patchIdsForStatus(String statusCode) {
        // TODO JHE (18.08.2020) : better map parameter
        String sql = "SELECT id FROM cm_patch_f p INNER JOIN cm_patch_status_f s ON p.status = s.pat_status WHERE s.pat_status = " + statusCode ;
        List<String> rows = jdbcTemplate.queryForList(sql,String.class);
        return rows;
    }
}
