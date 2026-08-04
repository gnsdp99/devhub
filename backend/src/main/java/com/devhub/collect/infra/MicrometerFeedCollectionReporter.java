package com.devhub.collect.infra;

import com.devhub.collect.app.port.out.FeedCollectionReporter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MicrometerFeedCollectionReporter implements FeedCollectionReporter {

    static final String RUN_TIMER = "devhub.collect.runs";
    static final String FEED_COUNTER = "devhub.collect.feeds";
    static final String ARTICLE_COUNTER = "devhub.collect.articles";

    private final MeterRegistry registry;

    @Override
    public Timing started() {
        Timer.Sample sample = Timer.start(registry);
        return () -> sample.stop(registry.timer(RUN_TIMER));
    }

    @Override
    public void collected(String feed, int stored) {
        countFeed(feed, "collected");
        registry.counter(ARTICLE_COUNTER, "feed", feed).increment(stored);
    }

    @Override
    public void unchanged(String feed) {
        countFeed(feed, "unchanged");
    }

    @Override
    public void failed(String feed) {
        countFeed(feed, "failed");
    }

    @Override
    public void errored(String feed) {
        countFeed(feed, "errored");
    }

    private void countFeed(String feed, String result) {
        registry.counter(FEED_COUNTER, "feed", feed, "result", result).increment();
    }
}