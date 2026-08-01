package com.devhub.article.domain;

import java.time.Instant;
import java.util.List;

public record Article(
        long id,
        String title,
        String url,
        String summary,
        String author,
        Instant publishedAt,
        List<ArticleSource> sources) {
}