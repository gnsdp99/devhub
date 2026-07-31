package com.devhub.article;

import java.time.Instant;

public record Article(
        long id,
        String title,
        String url,
        String summary,
        Instant publishedAt,
        String feed,
        String source,
        String sourceName) {
}
