package com.devhub.collect.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.devhub.article.domain.NewArticle;
import com.devhub.article.app.ArticleWriter;
import com.devhub.collect.domain.CollectionWindow;
import com.devhub.collect.domain.Feed;
import com.devhub.collect.domain.FeedFetchException;
import com.devhub.collect.domain.FeedParseException;
import com.devhub.collect.domain.FetchResult;
import com.devhub.collect.domain.ParsedArticle;
import com.devhub.collect.app.port.out.CollectionPolicy;
import com.devhub.collect.app.port.out.FeedCollectionExecutor;
import com.devhub.collect.app.port.out.FeedCollectionReporter;
import com.devhub.collect.app.port.out.FeedFetcher;
import com.devhub.collect.app.port.out.FeedParser;
import com.devhub.collect.app.port.out.FeedRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

@ExtendWith(MockitoExtension.class)
class FeedCollectorUnitTest {

    private static final Instant NOW = Instant.parse("2026-07-30T00:00:00Z");
    private static final Duration WINDOW = Duration.ofDays(30);
    private static final byte[] BODY = "<rss/>".getBytes();

    private static final FeedCollectionExecutor SEQUENTIAL = List::forEach;

    private static final CollectionPolicy POLICY = () -> new CollectionWindow(WINDOW);

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private ArticleWriter articleWriter;

    @Mock
    private FeedFetcher fetcher;

    @Mock
    private FeedParser parser;

    @Mock
    private FeedCollectionReporter reporter;

    private final AtomicBoolean timingFinished = new AtomicBoolean();

    private FeedCollector collector;

    @BeforeEach
    void setUp() {
        timingFinished.set(false);
        given(reporter.started()).willReturn(() -> timingFinished.set(true));
        collector = new FeedCollector(
                feedRepository,
                articleWriter,
                fetcher,
                parser,
                SEQUENTIAL,
                reporter,
                POLICY,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

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

        then(articleWriter).should(times(2)).write(anyList());
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
        then(articleWriter).shouldHaveNoInteractions();
        then(feedRepository).should().markUnchanged(1L);
        then(feedRepository).should(never()).markCollected(anyLong(), any(), any());
    }

    @Test
    @DisplayName("본문을 파싱하지 못하면 그 피드를 실패로 기록한다")
    void marksTheFeedFailedWhenParsingFails() {
        Feed feed = feed(1, "first");
        givenCollectible(feed);
        givenFetched(feed);
        given(parser.parse(eq(BODY), any())).willThrow(new FeedParseException("깨진 XML입니다.", null));

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
        given(articleWriter.write(anyList()))
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
    @DisplayName("피드마다 수집 결과를 보고한다")
    void reportsEachFeedOutcome() {
        Feed fresh = feed(1, "fresh");
        Feed notModified = feed(2, "not-modified");
        Feed broken = feed(3, "broken");
        givenCollectible(fresh, notModified, broken);
        givenFetched(fresh);
        given(fetcher.fetch(notModified.feedUrl(), null, null))
                .willReturn(new FetchResult.NotModified());
        given(fetcher.fetch(broken.feedUrl(), null, null))
                .willThrow(new FeedFetchException("피드 응답이 500입니다."));
        givenParsed(article("https://example.com/a", NOW));
        given(articleWriter.write(anyList())).willReturn(1);

        collector.collect();

        then(reporter).should().collected("fresh", 1);
        then(reporter).should().unchanged("not-modified");
        then(reporter).should().failed("broken");
    }

    @Test
    @DisplayName("DB 저장에 실패하면 오류로 보고한다")
    void reportsAnErrorWhenStoringFails() {
        Feed feed = feed(1, "first");
        givenCollectible(feed);
        givenFetched(feed);
        givenParsed(article("https://example.com/a", NOW));
        given(articleWriter.write(anyList()))
                .willThrow(new DataAccessResourceFailureException("연결이 끊겼습니다."));

        collector.collect();

        then(reporter).should().errored("first");
        then(reporter).should(never()).collected(any(), anyInt());
    }

    @Test
    @DisplayName("수집 도중 예외가 나도 소요 시간 측정을 끝낸다")
    void finishesTimingEvenWhenCollectingFails() {
        given(feedRepository.findCollectible())
                .willThrow(new DataAccessResourceFailureException("연결이 끊겼습니다."));

        assertThatThrownBy(() -> collector.collect())
                .isInstanceOf(DataAccessResourceFailureException.class);

        assertThat(timingFinished).isTrue();
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
        given(parser.parse(eq(BODY), any())).willReturn(List.of(articles));
    }

    private List<String> storedUrls() {
        ArgumentCaptor<List<NewArticle>> captor = ArgumentCaptor.captor();
        then(articleWriter).should().write(captor.capture());
        return captor.getValue().stream().map(NewArticle::url).toList();
    }

    private Feed feed(long id, String slug) {
        return new Feed(id, slug, "https://example.com/" + slug + ".xml", null, null);
    }

    private ParsedArticle article(String url, Instant publishedAt) {
        return new ParsedArticle("guid-" + url, url, "제목", null, null, publishedAt);
    }
}