package com.devhub.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.devhub.article.ArticleRepository;
import com.devhub.article.NewArticle;
import com.devhub.support.CollectPropertiesFixture;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataAccessResourceFailureException;

class FeedCollectorUnitTest {

    private static final Instant NOW = Instant.parse("2026-07-30T00:00:00Z");
    private static final Duration WINDOW = Duration.ofDays(30);
    private static final byte[] BODY = "<rss/>".getBytes();

    private final FeedRepository feedRepository = mock(FeedRepository.class);
    private final ArticleRepository articleRepository = mock(ArticleRepository.class);
    private final FeedFetcher fetcher = mock(FeedFetcher.class);
    private final FeedParser parser = mock(FeedParser.class);

    private final FeedCollector collector = new FeedCollector(
            feedRepository,
            articleRepository,
            fetcher,
            parser,
            CollectPropertiesFixture.of(WINDOW),
            Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    @DisplayName("한 피드가 실패해도 나머지 피드는 수집한다")
    void oneFailingFeedDoesNotStopTheOthers() {
        Feed first = feed(1, "first");
        Feed broken = feed(2, "broken");
        Feed last = feed(3, "last");
        givenCollectible(first, broken, last);
        givenFetched(first, last);
        given(fetcher.fetch(broken.feedUrl(), null, null))
                .willThrow(new FeedFetchException("피드 응답이 500입니다."));
        givenParsed(article("https://example.com/a", NOW));

        collector.collect();

        then(articleRepository).should(times(2)).insertNew(anyList());
        then(feedRepository).should().markFailed(2L);
        then(feedRepository).should(never()).markFailed(1L);
        then(feedRepository).should(never()).markFailed(3L);
        then(feedRepository).should().markCollected(1L, "\"v1\"", null);
        then(feedRepository).should().markCollected(3L, "\"v1\"", null);
    }

    @Test
    @DisplayName("304면 파싱하지 않고 변경 없음으로만 기록한다")
    void skipsParsingWhenNotModified() {
        Feed feed = feed(1, "first");
        givenCollectible(feed);
        given(fetcher.fetch(feed.feedUrl(), null, null)).willReturn(new FetchResult.NotModified());

        collector.collect();

        then(parser).shouldHaveNoInteractions();
        then(articleRepository).shouldHaveNoInteractions();
        then(feedRepository).should().markUnchanged(1L);
        then(feedRepository).should(never()).markCollected(anyLong(), any(), any());
    }

    @Test
    @DisplayName("본문을 파싱하지 못하면 그 피드를 실패로 기록한다")
    void marksTheFeedFailedWhenParsingFails() {
        Feed feed = feed(1, "first");
        givenCollectible(feed);
        givenFetched(feed);
        given(parser.parse(BODY)).willThrow(new FeedParseException("깨진 XML입니다.", null));

        collector.collect();

        then(feedRepository).should().markFailed(1L);
        then(feedRepository).should(never()).markCollected(anyLong(), any(), any());
    }

    @Test
    @DisplayName("DB 저장에 실패하면 피드의 상태를 건드리지 않는다")
    void doesNotBlameTheFeedWhenStoringFails() {
        Feed feed = feed(1, "first");
        givenCollectible(feed);
        givenFetched(feed);
        givenParsed(article("https://example.com/a", NOW));
        given(articleRepository.insertNew(anyList()))
                .willThrow(new DataAccessResourceFailureException("연결이 끊겼습니다."));

        collector.collect();

        then(feedRepository).should(never()).markCollected(anyLong(), any(), any());
        then(feedRepository).should(never()).markFailed(anyLong());
    }

    @Test
    @DisplayName("수집 윈도우보다 오래된 기사는 저장하지 않는다")
    void skipsArticlesOlderThanTheWindow() {
        Feed feed = feed(1, "first");
        givenCollectible(feed);
        givenFetched(feed);
        givenParsed(
                article("https://example.com/old", NOW.minus(WINDOW).minusSeconds(1)),
                article("https://example.com/new", NOW.minus(WINDOW).plusSeconds(1)));

        collector.collect();

        assertThat(storedUrls()).containsExactly("https://example.com/new");
    }

    @Test
    @DisplayName("해시할 수 없는 링크는 건너뛰고 나머지를 저장한다")
    void skipsArticlesWithAnUnhashableLink() {
        Feed feed = feed(1, "first");
        givenCollectible(feed);
        givenFetched(feed);
        givenParsed(
                article("mailto:editor@example.com", NOW),
                article("https://example.com/a", NOW));

        collector.collect();

        assertThat(storedUrls()).containsExactly("https://example.com/a");
    }

    @Test
    @DisplayName("동시에 가져오는 피드 수가 상한을 넘지 않는다")
    void fetchesNoMoreFeedsAtOnceThanTheLimit() {
        int concurrency = 2;
        Feed[] feeds = IntStream.rangeClosed(1, 10)
                .mapToObj(i -> feed(i, "feed-" + i))
                .toArray(Feed[]::new);
        givenCollectible(feeds);
        AtomicInteger inFlight = new AtomicInteger();
        AtomicInteger peak = new AtomicInteger();
        given(fetcher.fetch(any(), any(), any())).willAnswer(invocation -> {
            peak.accumulateAndGet(inFlight.incrementAndGet(), Math::max);
            Thread.sleep(20);
            inFlight.decrementAndGet();
            return new FetchResult.NotModified();
        });

        collectorWith(concurrency).collect();

        assertThat(peak.get()).isLessThanOrEqualTo(concurrency);
    }

    private FeedCollector collectorWith(int concurrency) {
        return new FeedCollector(
                feedRepository,
                articleRepository,
                fetcher,
                parser,
                CollectPropertiesFixture.of(WINDOW, concurrency),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private void givenCollectible(Feed... feeds) {
        given(feedRepository.findCollectible()).willReturn(List.of(feeds));
    }

    private void givenFetched(Feed... feeds) {
        for (Feed feed : feeds) {
            given(fetcher.fetch(feed.feedUrl(), null, null))
                    .willReturn(new FetchResult.Fetched(BODY, "\"v1\"", null));
        }
    }

    private void givenParsed(ParsedArticle... articles) {
        given(parser.parse(BODY)).willReturn(List.of(articles));
    }

    private List<String> storedUrls() {
        ArgumentCaptor<List<NewArticle>> captor = ArgumentCaptor.captor();
        then(articleRepository).should().insertNew(captor.capture());
        return captor.getValue().stream().map(NewArticle::url).toList();
    }

    private Feed feed(long id, String slug) {
        return new Feed(id, slug, "https://example.com/" + slug + ".xml", null, null);
    }

    private ParsedArticle article(String url, Instant publishedAt) {
        return new ParsedArticle("guid-" + url, url, "제목", null, null, publishedAt);
    }
}