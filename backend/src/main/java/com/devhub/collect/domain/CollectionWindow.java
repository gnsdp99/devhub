package com.devhub.collect.domain;

import java.time.Duration;
import java.time.Instant;

public record CollectionWindow(Duration duration) {

    public boolean includes(Instant publishedAt, Instant now) {
        return !publishedAt.isBefore(now.minus(duration));
    }
}