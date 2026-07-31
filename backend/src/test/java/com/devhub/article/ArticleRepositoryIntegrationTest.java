package com.devhub.article;

import static org.assertj.core.api.Assertions.assertThat;

import com.devhub.support.AbstractIntegrationTest;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
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
        @DisplayName("같은 글이 다른 소스에서도 오면 기사는 하나만 저장하고 두 소스 모두에서 볼 수 있다")
        void storesOneArticleAndShowsItUnderBothSources() {
            repository.insertNew(List.of(articleOf(feedId("hackernews"), URL)));

            repository.insertNew(List.of(articleOf(feedId("geeknews"), URL)));

            assertThat(countArticles()).isEqualTo(1);
            assertThat(repository.findPage(null, sourceId("hackernews"), 10)).hasSize(1);
            assertThat(repository.findPage(null, sourceId("geeknews"), 10)).hasSize(1);
        }

        @Test
        @DisplayName("한 소스의 두 피드가 같은 글을 실어도 그 소스의 목록에는 한 번만 나온다")
        void showsTheArticleOnceWhenTwoFeedsOfOneSourceCarryIt() {
            repository.insertNew(List.of(articleOf(feedId("google-deepmind"), URL)));

            repository.insertNew(List.of(articleOf(feedId("google-cloud"), URL)));

            assertThat(repository.findPage(null, sourceId("google"), 10)).hasSize(1);
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

            long sourceId = jdbcClient.sql("select source_id from article_source")
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

    @Nested
    @DisplayName("페이지 조회")
    class Page {

        @Test
        @DisplayName("최근에 발행된 기사부터 돌려준다")
        void returnsTheMostRecentlyPublishedArticlesFirst() {
            long feedId = feedId("hackernews");
            repository.insertNew(List.of(
                    articleOf(feedId, "https://example.com/old", PUBLISHED_AT.minusSeconds(60)),
                    articleOf(feedId, "https://example.com/new", PUBLISHED_AT)));

            List<Article> page = repository.findPage(null, null, 10);

            assertThat(page).extracting(Article::url)
                    .containsExactly("https://example.com/new", "https://example.com/old");
        }

        @Test
        @DisplayName("발행 시각이 같으면 나중에 저장한 기사부터 돌려준다")
        void breaksTiesOnPublishedAtByIdDescending() {
            long feedId = feedId("hackernews");
            repository.insertNew(List.of(
                    articleOf(feedId, "https://example.com/first", PUBLISHED_AT),
                    articleOf(feedId, "https://example.com/second", PUBLISHED_AT)));

            List<Article> page = repository.findPage(null, null, 10);

            assertThat(page).extracting(Article::url)
                    .containsExactly("https://example.com/second", "https://example.com/first");
        }

        @Test
        @DisplayName("limit보다 많이 돌려주지 않는다")
        void returnsAtMostTheGivenLimit() {
            long feedId = feedId("hackernews");
            repository.insertNew(List.of(
                    articleOf(feedId, "https://example.com/1", PUBLISHED_AT),
                    articleOf(feedId, "https://example.com/2", PUBLISHED_AT.minusSeconds(1)),
                    articleOf(feedId, "https://example.com/3", PUBLISHED_AT.minusSeconds(2))));

            assertThat(repository.findPage(null, null, 2)).hasSize(2);
        }

        @Test
        @DisplayName("커서보다 오래된 기사만 돌려준다")
        void returnsOnlyArticlesOlderThanTheCursor() {
            long feedId = feedId("hackernews");
            repository.insertNew(List.of(
                    articleOf(feedId, "https://example.com/1", PUBLISHED_AT),
                    articleOf(feedId, "https://example.com/2", PUBLISHED_AT.minusSeconds(1)),
                    articleOf(feedId, "https://example.com/3", PUBLISHED_AT.minusSeconds(2))));
            Article first = repository.findPage(null, null, 1).getFirst();

            List<Article> page = repository.findPage(ArticleCursor.of(first), null, 10);

            assertThat(page).extracting(Article::url)
                    .containsExactly("https://example.com/2", "https://example.com/3");
        }

        @Test
        @DisplayName("발행 시각이 같은 기사도 커서로 건너뛰거나 겹치지 않는다")
        void pagesThroughArticlesThatSharePublishedAt() {
            long feedId = feedId("hackernews");
            repository.insertNew(List.of(
                    articleOf(feedId, "https://example.com/1", PUBLISHED_AT),
                    articleOf(feedId, "https://example.com/2", PUBLISHED_AT),
                    articleOf(feedId, "https://example.com/3", PUBLISHED_AT)));
            List<Article> first = repository.findPage(null, null, 2);

            List<Article> second = repository.findPage(ArticleCursor.of(first.getLast()), null, 2);

            assertThat(Stream.concat(first.stream(), second.stream()))
                    .extracting(Article::url)
                    .containsExactly(
                            "https://example.com/3",
                            "https://example.com/2",
                            "https://example.com/1");
        }

        @Test
        @DisplayName("소스를 지정하면 그 소스의 기사만 돌려준다")
        void returnsOnlyArticlesOfTheGivenSource() {
            repository.insertNew(List.of(
                    articleOf(feedId("hackernews"), "https://example.com/hn", PUBLISHED_AT),
                    articleOf(
                            feedId("google-deepmind"),
                            "https://example.com/google",
                            PUBLISHED_AT.minusSeconds(1))));

            List<Article> page = repository.findPage(null, sourceId("google"), 10);

            assertThat(page).extracting(Article::url).containsExactly("https://example.com/google");
        }

        @Test
        @DisplayName("소스와 커서를 함께 걸면 둘 다 적용한다")
        void appliesBothTheSourceAndTheCursor() {
            long deepmind = feedId("google-deepmind");
            repository.insertNew(List.of(
                    articleOf(deepmind, "https://example.com/google-new", PUBLISHED_AT),
                    articleOf(deepmind, "https://example.com/google-old", PUBLISHED_AT.minusSeconds(60)),
                    articleOf(feedId("hackernews"), "https://example.com/hn", PUBLISHED_AT.minusSeconds(30))));
            Article newest = repository.findPage(null, sourceId("google"), 1).getFirst();

            List<Article> page = repository.findPage(ArticleCursor.of(newest), sourceId("google"), 10);

            assertThat(page).extracting(Article::url).containsExactly("https://example.com/google-old");
        }

        @Test
        @DisplayName("기사마다 피드와 소스 정보를 채운다")
        void fillsTheFeedAndSourceOfEachArticle() {
            repository.insertNew(List.of(articleOf(feedId("google-deepmind"), URL)));

            Article article = repository.findPage(null, null, 10).getFirst();

            assertThat(article.sources())
                    .containsExactly(new ArticleSource("google-deepmind", "google", "Google"));
        }

        @Test
        @DisplayName("여러 소스에 실린 기사는 소스를 이름순으로 모두 채운다")
        void listsEverySourceThatCarriedTheArticleOrderedByName() {
            repository.insertNew(List.of(articleOf(feedId("hackernews"), URL)));
            repository.insertNew(List.of(articleOf(feedId("geeknews"), URL)));

            Article article = repository.findPage(null, null, 10).getFirst();

            assertThat(article.sources())
                    .extracting(ArticleSource::sourceName)
                    .containsExactly("GeekNews", "Hacker News");
        }
    }

    private NewArticle articleOf(long feedId, String url) {
        return articleOf(feedId, url, PUBLISHED_AT);
    }

    private NewArticle articleOf(long feedId, String url, Instant publishedAt) {
        return new NewArticle(feedId, "guid-" + url, url, "제목", "요약", "작성자", publishedAt);
    }

    private long sourceId(String slug) {
        return jdbcClient.sql("select id from source where slug = :slug")
                .param("slug", slug)
                .query(Long.class)
                .single();
    }

    private long sourceIdOf(long feedId) {
        return jdbcClient.sql("select source_id from feed where id = :id")
                .param("id", feedId)
                .query(Long.class)
                .single();
    }

}
