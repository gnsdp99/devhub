package com.devhub.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devhub.support.UrlNormalizer;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NewArticleUnitTest {

    private static final Instant PUBLISHED_AT = Instant.parse("2026-07-20T12:00:00Z");

    @Test
    @DisplayName("url_hash를 url에서 직접 계산한다")
    void derivesUrlHashFromUrl() {
        String url = "https://blog.cloudflare.com/how-we-scaled-workers";

        NewArticle article = articleOf(url);

        assertThat(article.urlHash()).isEqualTo(UrlNormalizer.hash(url));
    }

    @Test
    @DisplayName("해시할 수 없는 URL이면 예외를 던진다")
    void rejectsAnUnhashableUrl() {
        assertThatThrownBy(() -> articleOf("mailto:editor@example.com"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private NewArticle articleOf(String url) {
        return new NewArticle(1L, null, url, "제목", null, null, PUBLISHED_AT);
    }
}