package com.devhub.article;

import com.devhub.support.UrlNormalizer;
import java.time.Instant;

public record NewArticle(
        long feedId,
        String guid,
        String url,
        byte[] urlHash,
        String title,
        String summary,
        String author,
        Instant publishedAt) {

    /**
     * @throws IllegalArgumentException 해시할 수 없는 URL이면
     */
    public NewArticle(
            long feedId,
            String guid,
            String url,
            String title,
            String summary,
            String author,
            Instant publishedAt) {
        this(feedId, guid, url, UrlNormalizer.hash(url), title, summary, author, publishedAt);
    }
}