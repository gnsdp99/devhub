package com.devhub.article;

public record ArticleSourceResponse(String feed, String source, String sourceName) {

    public static ArticleSourceResponse from(ArticleSource source) {
        return new ArticleSourceResponse(source.feed(), source.source(), source.sourceName());
    }
}
