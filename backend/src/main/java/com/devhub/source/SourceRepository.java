package com.devhub.source;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SourceRepository {

    private final JdbcClient jdbcClient;

    public List<Source> findEnabled() {
        return jdbcClient.sql("""
                        select slug, name, category, site_url
                          from source
                         where enabled
                         order by name collate "C"
                        """)
                .query(Source.class)
                .list();
    }

    /**
     * @return slug를 쓰는 활성 소스의 id. 없으면 빈 값
     */
    public Optional<Long> findEnabledIdBySlug(String slug) {
        return jdbcClient.sql("select id from source where slug = :slug and enabled")
                .param("slug", slug)
                .query(Long.class)
                .optional();
    }
}