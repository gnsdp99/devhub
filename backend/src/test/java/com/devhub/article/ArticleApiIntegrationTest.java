package com.devhub.article;

import static org.assertj.core.api.Assertions.assertThat;

import com.devhub.support.AbstractIntegrationTest;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@AutoConfigureMockMvc
class ArticleApiIntegrationTest extends AbstractIntegrationTest {

    private static final Instant PUBLISHED_AT = Instant.parse("2026-07-20T12:00:00Z");
    private static final String NEW_URL = "https://example.com/new";
    private static final String OLD_URL = "https://example.com/old";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArticleRepository repository;

    private MockMvcTester mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcTester.create(mockMvc);
    }

    @Test
    @DisplayName("기사 목록을 최근 발행 순으로 돌려준다")
    void returnsArticlesInReverseChronologicalOrder() {
        givenArticle(OLD_URL, PUBLISHED_AT.minusSeconds(60));
        givenArticle(NEW_URL, PUBLISHED_AT);

        assertThat(mvc.get().uri("/api/articles"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.items[*].url")
                .asArray()
                .containsExactly(NEW_URL, OLD_URL);
    }

    @Test
    @DisplayName("기사마다 제목, 링크, 요약, 발행 시각, 출처를 담아 돌려준다")
    void returnsTheFieldsOfEachArticle() {
        givenArticle(NEW_URL, PUBLISHED_AT);

        assertThat(mvc.get().uri("/api/articles"))
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.items[0].id", id -> assertThat(id).asNumber().isNotNull())
                .hasPathSatisfying("$.items[0].title", title -> assertThat(title).isEqualTo("제목"))
                .hasPathSatisfying("$.items[0].url", url -> assertThat(url).isEqualTo(NEW_URL))
                .hasPathSatisfying("$.items[0].summary", it -> assertThat(it).isEqualTo("요약"))
                .hasPathSatisfying(
                        "$.items[0].publishedAt", at -> assertThat(at).isEqualTo("2026-07-20T12:00:00Z"))
                .hasPathSatisfying(
                        "$.items[0].sources[0].feed",
                        it -> assertThat(it).isEqualTo("google-deepmind"))
                .hasPathSatisfying(
                        "$.items[0].sources[0].source", it -> assertThat(it).isEqualTo("google"))
                .hasPathSatisfying(
                        "$.items[0].sources[0].sourceName", it -> assertThat(it).isEqualTo("Google"));
    }

    @Test
    @DisplayName("남은 기사가 있으면 이어받을 커서를 준다")
    void givesACursorWhenMoreArticlesRemain() {
        givenArticle(OLD_URL, PUBLISHED_AT.minusSeconds(60));
        givenArticle(NEW_URL, PUBLISHED_AT);

        assertThat(mvc.get().uri("/api/articles").param("limit", "1"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.nextCursor")
                .isEqualTo(cursorOfNewest());
    }

    @Test
    @DisplayName("받은 커서로 다음 페이지를 이어받는다")
    void continuesFromTheGivenCursor() {
        givenArticle(OLD_URL, PUBLISHED_AT.minusSeconds(60));
        givenArticle(NEW_URL, PUBLISHED_AT);

        assertThat(mvc.get().uri("/api/articles").param("cursor", cursorOfNewest()))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.items[*].url")
                .asArray()
                .containsExactly(OLD_URL);
    }

    @Test
    @DisplayName("남은 기사가 없으면 커서를 주지 않는다")
    void givesNoCursorOnTheLastPage() {
        givenArticle(NEW_URL, PUBLISHED_AT);

        assertThat(mvc.get().uri("/api/articles"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.nextCursor")
                .isNull();
    }

    @Test
    @DisplayName("소스를 지정하면 그 소스의 기사만 돌려준다")
    void returnsOnlyArticlesOfTheGivenSource() {
        givenArticle(NEW_URL, PUBLISHED_AT);
        givenArticle("hackernews", OLD_URL, PUBLISHED_AT.minusSeconds(60));

        assertThat(mvc.get().uri("/api/articles").param("source", "hackernews"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.items[*].url")
                .asArray()
                .containsExactly(OLD_URL);
    }

    @Test
    @DisplayName("해석할 수 없는 커서면 400을 응답한다")
    void rejectsAnUndecodableCursor() {
        assertThat(mvc.get().uri("/api/articles").param("cursor", "!!!"))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("알 수 없는 소스면 400을 응답한다")
    void rejectsAnUnknownSource() {
        assertThat(mvc.get().uri("/api/articles").param("source", "없는소스"))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    private void givenArticle(String url, Instant publishedAt) {
        givenArticle("google-deepmind", url, publishedAt);
    }

    private void givenArticle(String feedSlug, String url, Instant publishedAt) {
        repository.insertNew(List.of(new NewArticle(
                feedId(feedSlug), "guid-" + url, url, "제목", "요약", "작성자", publishedAt)));
    }

    private String cursorOfNewest() {
        return ArticleCursor.of(repository.findPage(null, null, 1).getFirst()).encode();
    }
}