package com.apgsga.patch.db.integration.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class RdbmsConfig {

    @Value("${rdbms.oracle.url}")
    private String oracleJdbcUrl;

    @Value("${rdbms.oracle.user.name}")
    private String rdbmsUserName;

    @Value("${rdbms.oracle.user.pwd}")
    private String rdbmsUserPwd;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
        dataSource.setUrl(oracleJdbcUrl);
        dataSource.setUsername(rdbmsUserName);
        dataSource.setPassword(rdbmsUserPwd);
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        JdbcTemplate jt = new JdbcTemplate(dataSource());
        return jt;
    }

}
