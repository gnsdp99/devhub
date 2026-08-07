package com.devhub.collect.app;

import static com.devhub.support.StubHttpServer.respond;
import static org.assertj.core.api.Assertions.assertThat;

import com.devhub.support.CollectionContainerConfig;
import com.devhub.support.IntegrationTestSupport;
import com.devhub.support.StubHttpServer;
import com.devhub.support.TestAddressPolicyConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@Import({CollectionContainerConfig.class, TestAddressPolicyConfig.class})
@Sql(scripts = "/sql/reset-collection.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class FeedCollectionIntegrationTest extends IntegrationTestSupport {

    private static final String SOURCE_SLUG = "test-source";

    @Autowired
    private FeedCollector collector;

    private StubHttpServer server;

    @BeforeEach
    void startServerAndReplaceFeeds() throws IOException {
        server = StubHttpServer.start();

        update("update feed set enabled = false");
        updateTestSource("""
                insert into source (slug, name, enabled)
                values (:slug, '테스트 소스', true)
                """);
        addFeed("test-ok", server.serve("/ok", exchange -> {
            exchange.getResponseHeaders().set("ETag", "\"v1\"");
            respond(exchange, 200, feedBody());
        }));
        addFeed("test-broken",
                server.serve("/broken", exchange -> respond(exchange, 500, new byte[0])));
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }

    @Test
    @DisplayName("수집한 기사를 저장하고 실패한 피드만 실패로 기록한다")
    void storesArticlesAndRecordsOnlyTheFailingFeed() {
        collector.collect();

        assertThat(countArticles()).isEqualTo(2);
        assertThat(articleFeedSlug()).isEqualTo("test-ok");
        assertThat(articleSourceSlug()).isEqualTo(SOURCE_SLUG);

        assertThat(columnOf("test-ok", "etag", String.class)).isEqualTo("\"v1\"");
        assertThat(columnOf("test-ok", "consecutive_failures", Integer.class)).isZero();
        assertThat(columnOf("test-ok", "last_success_at", Instant.class)).isNotNull();

        assertThat(columnOf("test-broken", "consecutive_failures", Integer.class)).isEqualTo(1);
        assertThat(columnOf("test-broken", "last_success_at", Instant.class)).isNull();
    }

    @Test
    @DisplayName("같은 피드를 다시 수집해도 기사가 늘지 않는다")
    void collectingTwiceStoresTheSameArticlesOnce() {
        collector.collect();
        collector.collect();

        assertThat(countArticles()).isEqualTo(2);
    }

    private static byte[] feedBody() {
        String publishedAt = DateTimeFormatter.RFC_1123_DATE_TIME
                .format(ZonedDateTime.now(ZoneOffset.UTC));
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <rss version="2.0"><channel>
                  <title>테스트 피드</title>
                  <item>
                    <title>첫 번째 기사</title>
                    <link>https://example.com/first</link>
                    <pubDate>%s</pubDate>
                  </item>
                  <item>
                    <title>두 번째 기사</title>
                    <link>https://example.com/second</link>
                    <pubDate>%s</pubDate>
                  </item>
                </channel></rss>
                """.formatted(publishedAt, publishedAt).getBytes(StandardCharsets.UTF_8);
    }

    private void addFeed(String slug, String feedUrl) {
        jdbcClient.sql("""
                        insert into feed (source_id, slug, name, feed_url)
                        select id, :slug, :slug, :feedUrl from source where slug = :sourceSlug
                        """)
                .param("slug", slug)
                .param("feedUrl", feedUrl)
                .param("sourceSlug", SOURCE_SLUG)
                .update();
    }

    private <T> T columnOf(String feedSlug, String column, Class<T> type) {
        return jdbcClient.sql("select " + column + " from feed where slug = :slug")
                .param("slug", feedSlug)
                .query(type)
                .optional()
                .orElse(null);
    }

    private String articleFeedSlug() {
        return jdbcClient.sql("""
                        select distinct f.slug
                          from article_source asrc join feed f on f.id = asrc.feed_id
                        """)
                .query(String.class)
                .single();
    }

    private String articleSourceSlug() {
        return jdbcClient.sql("""
                        select distinct s.slug
                          from article_source asrc join source s on s.id = asrc.source_id
                        """)
                .query(String.class)
                .single();
    }

    private void updateTestSource(String sql) {
        jdbcClient.sql(sql).param("slug", SOURCE_SLUG).update();
    }
}