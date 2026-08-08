package com.devhub.collect.infra;

import com.devhub.collect.app.port.out.FeedCollectionExecutor;
import com.devhub.collect.domain.Feed;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VirtualThreadFeedCollectionExecutor implements FeedCollectionExecutor {

    private final FeedExecutionProperties properties;

    @Override
    public void runAll(List<Feed> feeds, Consumer<Feed> collectOne) {
        Semaphore total = new Semaphore(properties.concurrency());
        Map<String, Semaphore> perHost = new ConcurrentHashMap<>();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            feeds.forEach(feed -> executor.execute(() -> {
                Semaphore host = perHost.computeIfAbsent(
                        hostOf(feed.feedUrl()),
                        _ -> new Semaphore(properties.hostConcurrency()));
                runWithPermits(host, total, feed, collectOne);
            }));
        }
    }

    private void runWithPermits(
            Semaphore host, Semaphore total, Feed feed, Consumer<Feed> collectOne) {
        if (!acquire(host)) {
            return;
        }
        try {
            if (!acquire(total)) {
                return;
            }
            try {
                collectOne.accept(feed);
            } finally {
                total.release();
            }
        } finally {
            host.release();
        }
    }

    private static String hostOf(String feedUrl) {
        try {
            String host = URI.create(feedUrl).getHost();
            return host == null ? feedUrl : host.toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException e) {
            return feedUrl;
        }
    }

    private static boolean acquire(Semaphore permits) {
        try {
            permits.acquire();
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}