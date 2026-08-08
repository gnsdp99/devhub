package com.devhub.article.api;

import com.devhub.article.domain.Article;
import com.devhub.support.domain.Page;
import java.util.List;

public record ArticlePageResponse(List<ArticleResponse> items, String nextCursor) {

    public static ArticlePageResponse from(Page<Article> page) {
        return new ArticlePageResponse(
                page.items().stream().map(ArticleResponse::from).toList(), page.nextCursor());
    }
}
