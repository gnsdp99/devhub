package com.devhub.article.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.devhub.article.domain.Article;
import com.devhub.support.domain.TimeCursor;
import com.devhub.article.domain.ArticleSource;
import com.devhub.support.domain.InvalidCursorException;
import com.devhub.support.domain.Page;
import com.devhub.article.app.port.out.ArticlePagePolicy;
import com.devhub.article.app.port.out.ArticleRepository;
import com.devhub.source.app.SourceFinder;
import com.devhub.source.domain.UnknownSourceException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArticleFinderUnitTest {

    private static final Instant PUBLISHED_AT = Instant.parse("2026-07-20T12:00:00Z");
    private static final String SOURCE = "cloudflare";
    private static final String SOURCE_NAME = "Hacker News";

    private static final int DEFAULT_LIMIT = 20;
    private static final int MIN_LIMIT = 1;
    private static final int MAX_LIMIT = 50;

    @Mock
    private ArticleRepository repository;

    @Mock
    private SourceFinder sourceFinder;

    private ArticleFinder finder;

    @BeforeEach
    void setUp() {
        finder = new ArticleFinder(
                repository,
                sourceFinder,
                new FixedPagePolicy(DEFAULT_LIMIT, MIN_LIMIT, MAX_LIMIT));
    }

    @Nested
    @DisplayName("페이지 크기")
    class PageSize {

        @Test
        @DisplayName("limit이 없으면 기본값만큼 가져온다")
        void fallsBackToTheDefaultLimit() {
            finder.findPage(null, null, null);

            then(repository).should().findPage(null, null, DEFAULT_LIMIT + 1);
        }

        @Test
        @DisplayName("상한보다 큰 limit은 상한까지만 가져온다")
        void capsTheLimitAtTheMaximum() {
            finder.findPage(null, null, 1000);

            then(repository).should().findPage(null, null, MAX_LIMIT + 1);
        }

        @Test
        @DisplayName("0 이하인 limit은 하한까지 올린다")
        void raisesANonPositiveLimitToTheMinimum() {
            finder.findPage(null, null, 0);

            then(repository).should().findPage(null, null, MIN_LIMIT + 1);
        }
    }

    @Nested
    @DisplayName("다음 커서")
    class NextCursor {

        @Test
        @DisplayName("마지막으로 돌려준 기사의 발행 시각과 id로 만든다")
        void isEncodedFromThePublishedAtAndIdOfTheLastArticle() {
            Article last = givenFound(DEFAULT_LIMIT + 1).get(DEFAULT_LIMIT - 1);

            Page<Article> page = finder.findPage(null, null, null);

            assertThat(page.nextCursor())
                    .isEqualTo(new TimeCursor(last.publishedAt(), last.id()).encode());
        }
    }

    @Nested
    @DisplayName("받은 커서")
    class GivenCursor {

        @Test
        @DisplayName("해석해서 저장소에 넘긴다")
        void isDecodedBeforeReachingTheRepository() {
            TimeCursor cursor = new TimeCursor(PUBLISHED_AT, 7);

            finder.findPage(cursor.encode(), null, null);

            then(repository).should().findPage(cursor, null, DEFAULT_LIMIT + 1);
        }

        @Test
        @DisplayName("해석할 수 없으면 조회하지 않고 예외를 던진다")
        void skipsTheQueryWhenItCannotBeDecoded() {
            assertThatThrownBy(() -> finder.findPage("!!!", null, null))
                    .isInstanceOf(InvalidCursorException.class);

            then(repository).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("받은 소스")
    class GivenSource {

        @Test
        @DisplayName("slug를 소스 id로 바꿔 저장소에 넘긴다")
        void isTurnedIntoASourceIdBeforeReachingTheRepository() {
            given(sourceFinder.requireEnabledIdBySlug(SOURCE)).willReturn(7L);

            finder.findPage(null, SOURCE, null);

            then(repository).should().findPage(null, 7L, DEFAULT_LIMIT + 1);
        }

        @Test
        @DisplayName("그런 활성 소스가 없으면 조회하지 않고 예외를 던진다")
        void skipsTheQueryWhenNoSuchEnabledSourceExists() {
            given(sourceFinder.requireEnabledIdBySlug(SOURCE))
                    .willThrow(new UnknownSourceException(SOURCE));

            assertThatThrownBy(() -> finder.findPage(null, SOURCE, null))
                    .isInstanceOf(UnknownSourceException.class);

            then(repository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("소스를 지정하지 않으면 소스를 찾지 않는다")
        void isNotLookedUpWhenNoSourceIsGiven() {
            finder.findPage(null, null, null);

            then(sourceFinder).shouldHaveNoInteractions();
        }
    }

    private List<Article> givenFound(int count) {
        List<Article> articles = IntStream.rangeClosed(1, count)
                .mapToObj(i -> new Article(
                        i,
                        "제목 " + i,
                        "https://example.com/" + i,
                        null,
                        null,
                        PUBLISHED_AT.minusSeconds(i),
                        List.of(new ArticleSource(SOURCE, SOURCE, SOURCE_NAME))))
                .toList();
        given(repository.findPage(any(), any(), anyInt())).willReturn(articles);
        return articles;
    }

    private record FixedPagePolicy(int defaultSize, int minSize, int maxSize)
            implements ArticlePagePolicy {
    }
}