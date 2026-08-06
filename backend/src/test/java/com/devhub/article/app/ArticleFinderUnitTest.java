package com.devhub.article.app;

import static com.devhub.article.app.ArticleFinder.DEFAULT_LIMIT;
import static com.devhub.article.app.ArticleFinder.MAX_LIMIT;
import static com.devhub.article.app.ArticleFinder.MIN_LIMIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.devhub.article.domain.Article;
import com.devhub.article.domain.ArticleCursor;
import com.devhub.article.domain.ArticleSource;
import com.devhub.article.domain.InvalidCursorException;
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

    @Mock
    private ArticleRepository repository;

    @Mock
    private SourceFinder sourceFinder;

    private ArticleFinder finder;

    @BeforeEach
    void setUp() {
        finder = new ArticleFinder(repository, sourceFinder);
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
    @DisplayName("다음 페이지")
    class NextPage {

        @Test
        @DisplayName("한 페이지를 채우고도 남으면 마지막으로 돌려준 기사를 가리키는 커서를 준다")
        void pointsTheCursorAtTheLastReturnedArticle() {
            Article last = givenFound(DEFAULT_LIMIT + 1).get(DEFAULT_LIMIT - 1);

            ArticlePage page = finder.findPage(null, null, null);

            assertThat(page.items()).hasSize(DEFAULT_LIMIT);
            assertThat(page.nextCursor())
                    .isEqualTo(new ArticleCursor(last.publishedAt(), last.id()).encode());
        }

        @Test
        @DisplayName("남는 기사가 없으면 커서를 주지 않는다")
        void givesNoCursorOnTheLastPage() {
            givenFound(DEFAULT_LIMIT);

            ArticlePage page = finder.findPage(null, null, null);

            assertThat(page.items()).hasSize(DEFAULT_LIMIT);
            assertThat(page.nextCursor()).isNull();
        }
    }

    @Nested
    @DisplayName("받은 커서")
    class GivenCursor {

        @Test
        @DisplayName("해석해서 저장소에 넘긴다")
        void isDecodedBeforeReachingTheRepository() {
            ArticleCursor cursor = new ArticleCursor(PUBLISHED_AT, 7);

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
}