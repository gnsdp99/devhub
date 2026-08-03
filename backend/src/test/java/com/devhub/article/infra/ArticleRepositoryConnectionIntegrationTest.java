package com.devhub.article.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.devhub.article.domain.NewArticle;
import com.devhub.support.AbstractIntegrationTest;
import com.zaxxer.hikari.HikariDataSource;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ArticleRepositoryConnectionIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcArticleRepository repository;

    @Autowired
    private HikariDataSource dataSource;

    @AfterEach
    void clear() {
        update("delete from article_source");
        update("delete from article");
    }

    @Test
    @DisplayName("기사 목록을 조회한 뒤 커넥션을 반납한다")
    void returnsConnectionsAfterFindingPages() {
        repository.insertNew(List.of(new NewArticle(
                feedId("hackernews"),
                "guid",
                "https://example.com/article",
                "제목",
                "요약",
                "작성자",
                Instant.parse("2026-07-20T12:00:00Z"))));

        for (int i = 0; i < 3; i++) {
            assertThat(repository.findPage(null, null, 20)).hasSize(1);
        }

        assertThat(dataSource.getHikariPoolMXBean().getActiveConnections()).isZero();
    }
}