package com.devhub.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

@SpringBootTest
@Import(ContainerConfig.class)
public abstract class AbstractIntegrationTest {

    @Autowired
    protected JdbcClient jdbcClient;

    protected long count(String sql) {
        return jdbcClient.sql(sql).query(Long.class).single();
    }
}