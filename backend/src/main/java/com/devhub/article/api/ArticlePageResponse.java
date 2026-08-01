package com.devhub.article.api;

import com.devhub.article.app.ArticlePage;
import java.util.List;

public record ArticlePageResponse(List<ArticleResponse> items, String nextCursor) {

    public static ArticlePageResponse from(ArticlePage page) {
        return new ArticlePageResponse(
                page.items().stream().map(ArticleResponse::from).toList(), page.nextCursor());
    }
}
