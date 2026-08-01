package com.devhub.support.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class UrlNormalizerUnitTest {

    @Nested
    @DisplayName("같은 기사로 합쳐지는 경우")
    class Merged {

        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = {
                "https://example.com/post",
                "HTTPS://EXAMPLE.COM/post",
                "https://www.example.com/post",
                "http://example.com/post",
                "https://example.com:443/post",
                "http://example.com:80/post",
                "https://example.com/post/",
                "https://example.com/post#section",
                "  https://example.com/post  ",
        })
        @DisplayName("scheme, host, 기본 port, trailing slash, fragment를 정규화한다")
        void normalizesSchemeHostPortSlashAndFragment(String rawUrl) {
            assertThat(UrlNormalizer.normalize(rawUrl)).isEqualTo("https://example.com/post");
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = {
                "https://example.com",
                "https://example.com/",
                "https://www.example.com/",
        })
        @DisplayName("path가 없으면 루트로 정규화한다")
        void normalizesRootPath(String rawUrl) {
            assertThat(UrlNormalizer.normalize(rawUrl)).isEqualTo("https://example.com/");
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = {
                "https://example.com/post?utm_source=hn",
                "https://example.com/post?utm_source=hn&utm_medium=rss",
                "https://example.com/post?UTM_Source=hn",
                "https://example.com/post?fbclid=abc",
                "https://example.com/post?gclid=abc",
                "https://example.com/post?",
        })
        @DisplayName("추적용 query parameter만 있으면 query가 통째로 사라진다")
        void removesTrackingOnlyQuery(String rawUrl) {
            assertThat(UrlNormalizer.normalize(rawUrl)).isEqualTo("https://example.com/post");
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = {
                "https://example.com/post?id=7&fbclid=abc",
                "https://example.com/post?fbclid=abc&id=7",
                "https://example.com/post?&id=7",
        })
        @DisplayName("추적용 query parameter를 걷어내고 나머지는 남긴다")
        void keepsNonTrackingParameters(String rawUrl) {
            assertThat(UrlNormalizer.normalize(rawUrl)).isEqualTo("https://example.com/post?id=7");
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = {
                "https://example.com/post?a=1&b=2",
                "https://example.com/post?b=2&a=1",
        })
        @DisplayName("query parameter를 key 순으로 정렬한다")
        void sortsQueryParameters(String rawUrl) {
            assertThat(UrlNormalizer.normalize(rawUrl))
                    .isEqualTo("https://example.com/post?a=1&b=2");
        }

        @Test
        @DisplayName("같은 key가 여러 번 오면 원래 순서를 유지한다")
        void keepsOrderOfRepeatedKeys() {
            assertThat(UrlNormalizer.normalize("https://example.com/post?tag=b&tag=a&id=7"))
                    .isEqualTo("https://example.com/post?id=7&tag=b&tag=a");
        }
    }

    @Nested
    @DisplayName("다른 기사로 남는 경우")
    class NotMerged {

        @ParameterizedTest(name = "{0} != {1}")
        @CsvSource({
                "https://example.com/post?ref=hn, https://example.com/post?ref=twitter",
                "https://example.com/post?ref=hn, https://example.com/post",
                "https://example.com/post?source=a, https://example.com/post?source=b",
                "https://example.com/post?source=a, https://example.com/post",
                "https://example.com/post, https://example.com/Post",
                "https://example.com/post, https://blog.example.com/post",
                "https://example.com:8080/post, https://example.com/post",
        })
        @DisplayName("의미를 가질 수 있는 query parameter와 path 대소문자는 건드리지 않는다")
        void preservesMeaningfulDifferences(String one, String other) {
            assertThat(UrlNormalizer.normalize(one)).isNotEqualTo(UrlNormalizer.normalize(other));
        }
    }

    @Nested
    @DisplayName("거부하는 입력")
    class Rejected {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("비어 있는 URL")
        void blankUrl(String rawUrl) {
            assertThatThrownBy(() -> UrlNormalizer.normalize(rawUrl))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = {
                "/blog/post",
                "example.com/post",
                "mailto:hello@example.com",
                "javascript:alert(1)",
                "ftp://example.com/post",
                "https://",
                "http://exa mple.com/post",
        })
        @DisplayName("http 또는 https로 열 수 없는 URL")
        void unusableUrl(String rawUrl) {
            assertThatThrownBy(() -> UrlNormalizer.normalize(rawUrl))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("hash")
    class Hash {

        @Test
        @DisplayName("SHA-256 길이인 32바이트를 반환한다")
        void returnsThirtyTwoBytes() {
            assertThat(UrlNormalizer.hash("https://example.com/post")).hasSize(32);
        }

        @Test
        @DisplayName("정규화 결과가 같으면 같은 hash가 나온다")
        void sameNormalizedUrlYieldsSameHash() {
            assertThat(UrlNormalizer.hash("http://www.example.com/post/?utm_source=hn"))
                    .isEqualTo(UrlNormalizer.hash("https://example.com/post"));
        }

        @Test
        @DisplayName("정규화 결과가 다르면 다른 hash가 나온다")
        void differentNormalizedUrlYieldsDifferentHash() {
            assertThat(UrlNormalizer.hash("https://example.com/post"))
                    .isNotEqualTo(UrlNormalizer.hash("https://example.com/other"));
        }
    }
}
