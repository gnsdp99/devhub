package com.devhub.collect.domain;

import java.time.Duration;
import java.time.Instant;

public final class CollectionWindow {

    private static final Duration DURATION = Duration.ofDays(30);

    private CollectionWindow() {
    }

    public static boolean includes(Instant publishedAt, Instant now) {
        return !publishedAt.isBefore(now.minus(DURATION));
    }
}
