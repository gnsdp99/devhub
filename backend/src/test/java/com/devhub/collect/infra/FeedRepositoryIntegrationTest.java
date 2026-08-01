package com.devhub.collect.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.devhub.collect.domain.Feed;
import com.devhub.support.AbstractIntegrationTest;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class FeedRepositoryIntegrationTest extends AbstractIntegrationTest {

    private static final String LAST_MODIFIED = "Wed, 29 Jul 2026 07:18:24 GMT";

    @Autowired
    private FeedRepository repository;

    @Nested
    @DisplayName("수집 대상 조회")
    class FindCollectible {

        @Test
        @DisplayName("피드가 비활성이면 제외된다")
        void skipsDisabledFeeds() {
            update("update feed set enabled = false where slug = 'hackernews'");

            assertThat(slugsOf(repository.findCollectible())).doesNotContain("hackernews");
        }

        @Test
        @DisplayName("소스가 비활성이면 그 소스의 피드가 전부 제외된다")
        void skipsEveryFeedOfADisabledSource() {
            update("update source set enabled = false where slug = 'google'");

            assertThat(slugsOf(repository.findCollectible()))
                    .doesNotContain("google-keyword", "google-deepmind")
                    .contains("hackernews");
        }

        @Test
        @DisplayName("조건부 요청에 실어 보낼 etag와 last_modified를 함께 읽는다")
        void readsStoredConditionalRequestHeaders() {
            repository.markCollected(feedId("hackernews"), "\"v1\"", LAST_MODIFIED);

            Feed feed = feedOf("hackernews");

            assertThat(feed.feedUrl()).isEqualTo("https://hnrss.org/frontpage");
            assertThat(feed.etag()).isEqualTo("\"v1\"");
            assertThat(feed.lastModified()).isEqualTo(LAST_MODIFIED);
        }
    }

    @Nested
    @DisplayName("수집 결과 기록")
    class Mark {

        @Test
        @DisplayName("수집에 성공하면 조건부 요청 헤더와 성공 시각을 남긴다")
        void markCollectedStoresHeadersAndSuccessTime() {
            long feedId = feedId("hackernews");

            repository.markCollected(feedId, "\"v1\"", LAST_MODIFIED);

            assertThat(feedOf("hackernews").etag()).isEqualTo("\"v1\"");
            assertThat(instantOf(feedId, "last_success_at")).isNotNull();
        }

        @Test
        @DisplayName("응답에 헤더가 없으면 저장해 둔 값을 지운다")
        void markCollectedClearsHeadersTheResponseOmitted() {
            long feedId = feedId("hackernews");
            repository.markCollected(feedId, "\"v1\"", LAST_MODIFIED);

            repository.markCollected(feedId, null, null);

            assertThat(feedOf("hackernews").etag()).isNull();
            assertThat(feedOf("hackernews").lastModified()).isNull();
        }

        @Test
        @DisplayName("304면 조건부 요청 헤더를 그대로 두고 성공 시각만 갱신한다")
        void markUnchangedKeepsHeaders() {
            long feedId = feedId("hackernews");
            repository.markCollected(feedId, "\"v1\"", LAST_MODIFIED);

            repository.markUnchanged(feedId);

            assertThat(feedOf("hackernews").etag()).isEqualTo("\"v1\"");
            assertThat(instantOf(feedId, "last_success_at")).isNotNull();
        }

        @Test
        @DisplayName("실패하면 연속 실패 횟수만 올리고 성공 시각은 두지 않는다")
        void markFailedIncrementsTheFailureCount() {
            long feedId = feedId("hackernews");

            repository.markFailed(feedId);
            repository.markFailed(feedId);

            assertThat(failureCountOf(feedId)).isEqualTo(2);
            assertThat(instantOf(feedId, "last_success_at")).isNull();
            assertThat(instantOf(feedId, "last_fetched_at")).isNotNull();
        }

        @Test
        @DisplayName("다시 성공하면 연속 실패 횟수가 0으로 돌아간다")
        void successResetsTheFailureCount() {
            long feedId = feedId("hackernews");
            repository.markFailed(feedId);

            repository.markCollected(feedId, null, null);

            assertThat(failureCountOf(feedId)).isZero();
        }

        @Test
        @DisplayName("피드를 갱신하면 updated_at이 따라 갱신된다")
        void updatingAFeedMovesUpdatedAt() {
            long feedId = feedId("hackernews");
            Instant before = instantOf(feedId, "updated_at");

            repository.markFailed(feedId);

            assertThat(instantOf(feedId, "updated_at")).isAfter(before);
        }
    }

    private List<String> slugsOf(List<Feed> feeds) {
        return feeds.stream().map(Feed::slug).toList();
    }

    private Feed feedOf(String slug) {
        return repository.findCollectible().stream()
                .filter(feed -> feed.slug().equals(slug))
                .findFirst()
                .orElseThrow();
    }

    private int failureCountOf(long feedId) {
        return jdbcClient.sql("select consecutive_failures from feed where id = :id")
                .param("id", feedId)
                .query(Integer.class)
                .single();
    }

    private Instant instantOf(long feedId, String column) {
        return jdbcClient.sql("select " + column + " from feed where id = :id")
                .param("id", feedId)
                .query(Instant.class)
                .optional()
                .orElse(null);
    }
}