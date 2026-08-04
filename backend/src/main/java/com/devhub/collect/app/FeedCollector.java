package com.devhub.collect.app;

import com.devhub.article.domain.NewArticle;
import com.devhub.article.app.ArticleWriter;
import com.devhub.collect.domain.CollectionWindow;
import com.devhub.collect.domain.Feed;
import com.devhub.collect.domain.FeedFetchException;
import com.devhub.collect.domain.FeedParseException;
import com.devhub.collect.domain.FetchResult;
import com.devhub.collect.domain.ParsedArticle;
import com.devhub.collect.app.port.out.FeedCollectionExecutor;
import com.devhub.collect.app.port.out.FeedCollectionReporter;
import com.devhub.collect.app.port.out.FeedFetcher;
import com.devhub.collect.app.port.out.FeedParser;
import com.devhub.collect.app.port.out.FeedRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedCollector {

    private final FeedRepository feedRepository;
    private final ArticleWriter articleWriter;
    private final FeedFetcher fetcher;
    private final FeedParser parser;
    private final FeedCollectionExecutor executor;
    private final FeedCollectionReporter reporter;
    private final Clock clock;

    public void collect() {
        try (var _ = reporter.started()) {
            List<Feed> feeds = feedRepository.findCollectible();
            log.info("피드 {}개를 수집합니다.", feeds.size());
            executor.runAll(feeds, this::collectOne);
        }
    }

    private void collectOne(Feed feed) {
        try {
            FetchResult result = fetcher.fetch(feed.feedUrl(), feed.etag(), feed.lastModified());
            switch (result) {
                case FetchResult.NotModified _ -> {
                    feedRepository.markUnchanged(feed.id());
                    reporter.unchanged(feed.slug());
                }
                case FetchResult.Fetched fetched -> store(feed, fetched);
            }
        } catch (FeedFetchException | FeedParseException e) {
            feedRepository.markFailed(feed.id());
            reporter.failed(feed.slug());
            log.warn("피드 수집에 실패했습니다. slug={}", feed.slug(), e);
        } catch (RuntimeException e) {
            reporter.errored(feed.slug());
            log.error("피드를 저장하지 못했습니다. slug={}", feed.slug(), e);
        }
    }

    private void store(Feed feed, FetchResult.Fetched fetched) {
        List<NewArticle> articles = toNewArticles(feed, parser.parse(fetched.body()));
        int stored = articleWriter.write(articles);
        feedRepository.markCollected(feed.id(), fetched.etag(), fetched.lastModified());
        reporter.collected(feed.slug(), stored);
        log.info("피드를 수집했습니다. slug={}, 신규={}건", feed.slug(), stored);
    }

    private List<NewArticle> toNewArticles(Feed feed, List<ParsedArticle> parsed) {
        Instant now = clock.instant();
        List<NewArticle> articles = new ArrayList<>();
        for (ParsedArticle article : parsed) {
            if (!CollectionWindow.includes(article.publishedAt(), now)) {
                continue;
            }
            try {
                articles.add(new NewArticle(
                        feed.id(),
                        article.guid(),
                        article.url(),
                        article.title(),
                        article.summary(),
                        article.author(),
                        article.publishedAt()));
            } catch (IllegalArgumentException e) {
                log.warn("URL을 해시할 수 없어 건너뜁니다. slug={}, url={}", feed.slug(), article.url(), e);
            }
        }
        return articles;
    }
}
