package com.devhub.collect.api;

import com.devhub.collect.app.FeedCollector;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "devhub.collect.enabled", havingValue = "true")
public class FeedCollectionScheduler {

    private final FeedCollector collector;

    @Scheduled(fixedDelayString = "${devhub.collect.interval}")
    public void collect() {
        collector.collect();
    }
}