package com.devhub.collect;

import com.devhub.article.ArticleRepository;
import com.devhub.article.NewArticle;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedCollector {

    private final FeedRepository feedRepository;
    private final ArticleRepository articleRepository;
    private final FeedFetcher fetcher;
    private final FeedParser parser;
    private final CollectProperties properties;
    private final Clock clock;

    public void collect() {
        List<Feed> feeds = feedRepository.findCollectible();
        log.info("피드 {}개를 수집합니다.", feeds.size());
        Semaphore permits = new Semaphore(properties.concurrency());
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            feeds.forEach(feed -> executor.execute(() -> collectWithin(permits, feed)));
        }
    }

    private void collectWithin(Semaphore permits, Feed feed) {
        try {
            permits.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        try {
            collectOne(feed);
        } finally {
            permits.release();
        }
    }

    private void collectOne(Feed feed) {
        try {
            FetchResult result = fetcher.fetch(feed.feedUrl(), feed.etag(), feed.lastModified());
            switch (result) {
                case FetchResult.NotModified ignored -> feedRepository.markUnchanged(feed.id());
                case FetchResult.Fetched fetched -> store(feed, fetched);
            }
        } catch (FeedFetchException | FeedParseException e) {
            feedRepository.markFailed(feed.id());
            log.warn("피드 수집에 실패했습니다. slug={}", feed.slug(), e);
        } catch (RuntimeException e) {
            log.error("피드를 저장하지 못했습니다. slug={}", feed.slug(), e);
        }
    }

    private void store(Feed feed, FetchResult.Fetched fetched) {
        List<NewArticle> articles = toNewArticles(feed, parser.parse(fetched.body()));
        int stored = articleRepository.insertNew(articles);
        feedRepository.markCollected(feed.id(), fetched.etag(), fetched.lastModified());
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