package com.devhub.article;

import static org.assertj.core.api.Assertions.assertThat;

import com.devhub.support.AbstractIntegrationTest;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class ArticleRepositoryIntegrationTest extends AbstractIntegrationTest {

    private static final Instant PUBLISHED_AT = Instant.parse("2026-07-20T12:00:00Z");
    private static final String URL = "https://blog.cloudflare.com/how-we-scaled-workers";

    @Autowired
    private ArticleRepository repository;

    @Nested
    @DisplayName("저장")
    class Insert {

        @Test
        @DisplayName("기사를 저장하고 저장한 건수를 돌려준다")
        void insertsArticlesAndReturnsTheStoredCount() {
            int stored = repository.insertNew(List.of(
                    articleOf(feedId("hackernews"), URL),
                    articleOf(feedId("hackernews"), "https://example.com/other")));

            assertThat(stored).isEqualTo(2);
            assertThat(countArticles()).isEqualTo(2);
        }

        @Test
        @DisplayName("guid, summary, author가 없어도 저장한다")
        void insertsArticlesWithoutOptionalFields() {
            NewArticle article =
                    new NewArticle(feedId("hackernews"), null, URL, "제목", null, null, PUBLISHED_AT);

            assertThat(repository.insertNew(List.of(article))).isEqualTo(1);
        }

        @Test
        @DisplayName("빈 목록이면 쿼리를 보내지 않는다")
        void doesNothingForAnEmptyList() {
            assertThat(repository.insertNew(List.of())).isZero();
        }
    }

    @Nested
    @DisplayName("중복 판정")
    class Duplicate {

        @Test
        @DisplayName("같은 기사를 다시 저장해도 건수가 늘지 않는다")
        void storesTheSameArticleOnlyOnce() {
            long feedId = feedId("hackernews");
            repository.insertNew(List.of(articleOf(feedId, URL)));

            int stored = repository.insertNew(List.of(articleOf(feedId, URL)));

            assertThat(stored).isZero();
            assertThat(countArticles()).isEqualTo(1);
        }

        @Test
        @DisplayName("한 번의 호출 안에 같은 URL이 두 번 있어도 한 건만 저장한다")
        void storesOnlyOneRowForDuplicatesInsideOneCall() {
            long feedId = feedId("hackernews");

            int stored = repository.insertNew(
                    List.of(articleOf(feedId, URL), articleOf(feedId, URL)));

            assertThat(stored).isEqualTo(1);
            assertThat(countArticles()).isEqualTo(1);
        }

        @Test
        @DisplayName("tracking parameter만 다른 URL은 같은 기사로 본다")
        void treatsUrlsDifferingOnlyByTrackingParameterAsOneArticle() {
            long feedId = feedId("hackernews");
            repository.insertNew(List.of(articleOf(feedId, URL)));

            int stored = repository.insertNew(
                    List.of(articleOf(feedId, URL + "?utm_source=newsletter")));

            assertThat(stored).isZero();
        }

        @Test
        @DisplayName("다른 피드에서 온 같은 기사도 한 건만 남고 먼저 수집한 피드가 출처로 남는다")
        void keepsTheFirstFeedAsTheSourceOfAnArticleSeenTwice() {
            long hackernews = feedId("hackernews");
            repository.insertNew(List.of(articleOf(hackernews, URL)));

            int stored = repository.insertNew(List.of(articleOf(feedId("geeknews"), URL)));

            assertThat(stored).isZero();
            assertThat(storedFeedId()).isEqualTo(hackernews);
        }
    }

    @Nested
    @DisplayName("컬럼 값")
    class Columns {

        @Test
        @DisplayName("source_id를 feed 행에서 가져와 채운다")
        void takesSourceIdFromTheFeedRow() {
            long feedId = feedId("google-deepmind");
            repository.insertNew(List.of(articleOf(feedId, URL)));

            long sourceId = jdbcClient.sql("select source_id from article")
                    .query(Long.class)
                    .single();

            assertThat(sourceId).isEqualTo(sourceIdOf(feedId));
        }

        @Test
        @DisplayName("published_at을 밀리초로 잘라 저장한다")
        void truncatesPublishedAtToMilliseconds() {
            Instant withMicros = Instant.parse("2026-07-20T12:00:00.123456Z");
            repository.insertNew(List.of(new NewArticle(
                    feedId("hackernews"), null, URL, "제목", null, null, withMicros)));

            Instant stored = jdbcClient.sql("select published_at from article")
                    .query(Instant.class)
                    .single();

            assertThat(stored).isEqualTo(Instant.parse("2026-07-20T12:00:00.123Z"));
        }
    }

    private NewArticle articleOf(long feedId, String url) {
        return new NewArticle(feedId, "guid-" + url, url, "제목", "요약", "작성자", PUBLISHED_AT);
    }

    private long sourceIdOf(long feedId) {
        return jdbcClient.sql("select source_id from feed where id = :id")
                .param("id", feedId)
                .query(Long.class)
                .single();
    }

    private long storedFeedId() {
        return jdbcClient.sql("select feed_id from article").query(Long.class).single();
    }
}
