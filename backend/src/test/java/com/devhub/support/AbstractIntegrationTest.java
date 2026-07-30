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

    protected long feedId(String slug) {
        return jdbcClient.sql("select id from feed where slug = :slug")
                .param("slug", slug)
                .query(Long.class)
                .single();
    }

    protected long countArticles() {
        return count("select count(*) from article");
    }

    protected long count(String sql) {
        return jdbcClient.sql(sql).query(Long.class).single();
    }
}