package com.devhub.collect.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.devhub.collect.domain.Feed;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VirtualThreadFeedCollectionExecutorUnitTest {

    private static final List<Feed> FEEDS = IntStream.rangeClosed(1, 10)
            .mapToObj(i -> new Feed(i, "feed-" + i, "https://example.com/" + i + ".xml", null, null))
            .toList();

    @Test
    @DisplayName("동시에 실행하는 작업 수가 상한을 넘지 않는다")
    void runsNoMoreTasksAtOnceThanTheLimit() {
        int concurrency = 2;
        AtomicInteger inFlight = new AtomicInteger();
        AtomicInteger peak = new AtomicInteger();

        executorWith(concurrency).runAll(FEEDS, feed -> {
            peak.accumulateAndGet(inFlight.incrementAndGet(), Math::max);
            sleep();
            inFlight.decrementAndGet();
        });

        assertThat(peak.get()).isLessThanOrEqualTo(concurrency);
    }

    @Test
    @DisplayName("모든 피드에 대해 한 번씩 실행한다")
    void runsEveryFeedExactlyOnce() {
        List<String> ran = new CopyOnWriteArrayList<>();

        executorWith(4).runAll(FEEDS, feed -> ran.add(feed.slug()));

        assertThat(ran).containsExactlyInAnyOrderElementsOf(FEEDS.stream().map(Feed::slug).toList());
    }

    private VirtualThreadFeedCollectionExecutor executorWith(int concurrency) {
        return new VirtualThreadFeedCollectionExecutor(new FeedExecutionProperties(concurrency));
    }

    private void sleep() {
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}