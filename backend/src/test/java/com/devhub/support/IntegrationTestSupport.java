package com.devhub.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

public abstract class IntegrationTestSupport {

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

    protected void update(String sql) {
        jdbcClient.sql(sql).update();
    }
}