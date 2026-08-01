package com.devhub.collect.infra;

import com.devhub.collect.app.port.out.FeedCollectionExecutor;
import com.devhub.collect.domain.Feed;
import java.util.List;
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
        Semaphore permits = new Semaphore(properties.concurrency());
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            feeds.forEach(feed -> executor.execute(() -> runWithPermit(permits, feed, collectOne)));
        }
    }

    private void runWithPermit(Semaphore permits, Feed feed, Consumer<Feed> collectOne) {
        try {
            permits.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        try {
            collectOne.accept(feed);
        } finally {
            permits.release();
        }
    }
}