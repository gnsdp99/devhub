package com.devhub.collect.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devhub.collect.domain.FeedParseException;
import com.devhub.collect.domain.ParsedArticle;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class FeedParserUnitTest {

    private static final Instant NOW = Instant.parse("2026-07-30T00:00:00Z");

    private final FeedParser parser = new FeedParser(Clock.fixed(NOW, ZoneOffset.UTC));

    private List<ParsedArticle> parseFixture(String name) {
        try {
            return parser.parse(new ClassPathResource("feeds/" + name).getContentAsByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Nested
    @DisplayName("RSS 2.0 (Hacker News)")
    class Rss {

        private final List<ParsedArticle> articles = parseFixture("hnrss.xml");

        @Test
        @DisplayName("item을 모두 읽는다")
        void readsEveryItem() {
            assertThat(articles).hasSize(3);
        }

        @Test
        @DisplayName("link를 url로, guid를 guid로 각각 따로 읽는다")
        void readsLinkAndGuidSeparately() {
            ParsedArticle first = articles.getFirst();
            assertThat(first.url()).startsWith("https://micrologics.org/blog/");
            assertThat(first.guid()).isEqualTo("https://news.ycombinator.com/item?id=49094346");
        }

        @Test
        @DisplayName("dc:creator를 author로 읽는다")
        void readsDublinCoreCreator() {
            assertThat(articles.getFirst().author()).isEqualTo("ankitg12");
        }

        @Test
        @DisplayName("pubDate를 그대로 읽는다")
        void readsPubDate() {
            assertThat(articles.getFirst().publishedAt())
                    .isEqualTo(Instant.parse("2026-07-29T07:18:24Z"));
        }

        @Test
        @DisplayName("description의 HTML을 걷어내고 평문만 남긴다")
        void stripsHtmlFromDescription() {
            String summary = articles.getFirst().summary();
            assertThat(summary).doesNotContain("<p>", "</p>", "<a ").contains("Points: 51");
        }
    }

    @Nested
    @DisplayName("Atom (GeekNews)")
    class Atom {

        private final List<ParsedArticle> articles = parseFixture("geeknews.xml");

        @Test
        @DisplayName("entry를 모두 읽는다")
        void readsEveryEntry() {
            assertThat(articles).hasSize(3);
        }

        @Test
        @DisplayName("alternate link를 url로 읽는다")
        void readsAlternateLink() {
            assertThat(articles.getFirst().url()).isEqualTo("https://news.hada.io/topic?id=31947");
        }

        @Test
        @DisplayName("author 이름을 읽는다")
        void readsAuthorName() {
            assertThat(articles.getFirst().author()).isEqualTo("neo");
        }

        @Test
        @DisplayName("published를 읽는다")
        void readsPublished() {
            assertThat(articles.getFirst().publishedAt())
                    .isEqualTo(Instant.parse("2026-07-29T08:33:31Z"));
        }

        @Test
        @DisplayName("summary가 없으면 content의 HTML을 걷어내 쓴다")
        void fallsBackToContent() {
            String summary = articles.getFirst().summary();
            assertThat(summary).doesNotContain("<li>", "<strong>").contains("Slow News");
        }
    }

    @Nested
    @DisplayName("발행 시각과 content가 온전하지 않은 피드 (요즘IT)")
    class Incomplete {

        private final List<ParsedArticle> articles = parseFixture("yozm.xml");

        @Test
        @DisplayName("pubDate가 없으면 수집 시각으로 채운다")
        void fillsMissingPubDateWithNow() {
            assertThat(articles).isNotEmpty().allSatisfy(
                    article -> assertThat(article.publishedAt()).isEqualTo(NOW));
        }

        @Test
        @DisplayName("content:encoded가 깨져 있어도 description을 쓴다")
        void prefersDescriptionOverContent() {
            assertThat(articles.getFirst().summary())
                    .startsWith("매달 마감이면")
                    .doesNotContain("CDATA");
        }
    }

    @Nested
    @DisplayName("망가진 항목")
    class Broken {

        private List<ParsedArticle> parse(String xml) {
            return parser.parse(xml.getBytes(StandardCharsets.UTF_8));
        }

        @Test
        @DisplayName("발행 시각이 미래면 수집 시각으로 당긴다")
        void clampsFuturePublishedAt() {
            List<ParsedArticle> articles = parse(rss("""
                    <item>
                      <title>내일 올라온 글</title>
                      <link>https://example.com/future</link>
                      <pubDate>Fri, 31 Jul 2026 00:00:00 +0000</pubDate>
                    </item>"""));

            assertThat(articles.getFirst().publishedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("link나 title이 없는 항목만 건너뛰고 나머지는 살린다")
        void skipsOnlyItemsWithoutLinkOrTitle() {
            List<ParsedArticle> articles = parse(rss("""
                    <item><title>링크가 없다</title></item>
                    <item><link>https://example.com/no-title</link></item>
                    <item>
                      <title>멀쩡한 글</title>
                      <link>https://example.com/ok</link>
                    </item>"""));

            assertThat(articles).singleElement()
                    .extracting(ParsedArticle::url)
                    .isEqualTo("https://example.com/ok");
        }

        @Test
        @DisplayName("XML이 아니면 FeedParseException을 던진다")
        void rejectsNonXml() {
            assertThatThrownBy(() -> parse("<html><body>404 Not Found</body></html>"))
                    .isInstanceOf(FeedParseException.class);
        }

        @Test
        @DisplayName("DOCTYPE이 있으면 외부 엔티티를 읽지 않고 거부한다")
        void rejectsDoctype() {
            assertThatThrownBy(() -> parse("""
                    <?xml version="1.0" encoding="UTF-8"?>
                    <!DOCTYPE rss [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
                    <rss version="2.0"><channel><title>&xxe;</title><link>https://example.com</link>
                    <description>d</description></channel></rss>"""))
                    .isInstanceOf(FeedParseException.class);
        }

        private String rss(String items) {
            return """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <rss version="2.0"><channel><title>t</title><link>https://example.com</link>
                    <description>d</description>%s</channel></rss>""".formatted(items);
        }
    }
}