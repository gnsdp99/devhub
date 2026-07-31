package com.devhub.article;

import java.time.Instant;
import java.util.List;

public record Article(
        long id,
        String title,
        String url,
        String summary,
        Instant publishedAt,
        List<ArticleSource> sources) {
}