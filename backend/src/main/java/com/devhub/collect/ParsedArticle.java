package com.devhub.collect;

import java.time.Instant;

public record ParsedArticle(
        String guid,
        String url,
        String title,
        String summary,
        String author,
        Instant publishedAt) {
}