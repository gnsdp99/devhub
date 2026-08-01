package com.devhub.article.api;

import com.devhub.article.domain.Article;
import java.time.Instant;
import java.util.List;

public record ArticleResponse(
        long id,
        String title,
        String url,
        String summary,
        String author,
        Instant publishedAt,
        List<ArticleSourceResponse> sources) {

    public static ArticleResponse from(Article article) {
        return new ArticleResponse(
                article.id(),
                article.title(),
                article.url(),
                article.summary(),
                article.author(),
                article.publishedAt(),
                article.sources().stream().map(ArticleSourceResponse::from).toList());
    }
}
