package com.apgsga.patch.db.integration.tests;

import com.apgsga.patch.db.integration.config.RdbmsConfig;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = RdbmsConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class RdbmsSimpleTest {

    @Autowired
    private JdbcTemplate jt;

    @Test
    // JHE (17.08.2020) : Ignoring it since it requires DB pre-requisite to work
    //					  Also the following properties have to be correctly defined into application-test.properties
    //							rdbms.oracle.url
    //							rdbms.oracle.user.name
    //							rdbms.oracle.user.pwd
    @Ignore
    public void simpleOracleDbTest() {
        Assert.notNull(jt,"jt (JdbcTemplate) is null");
        List<Map<String, Object>> rows = jt.queryForList("SELECT * from cm.cm_patch_f where id > 7000");
        for (Map row : rows) {
            System.out.println(row.get("ID"));
        }
    }
}
