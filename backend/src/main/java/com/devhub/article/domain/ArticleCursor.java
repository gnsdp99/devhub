package com.devhub.article.domain;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

public record ArticleCursor(Instant publishedAt, long id) {

    private static final String SEPARATOR = ":";

    public static ArticleCursor of(Article article) {
        return new ArticleCursor(article.publishedAt(), article.id());
    }

    /**
     * @throws InvalidCursorException 커서를 해석할 수 없으면
     */
    public static ArticleCursor decode(String encoded) {
        try {
            String decoded = new String(
                    Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
            int separator = decoded.indexOf(SEPARATOR);
            if (separator == -1) {
                throw new IllegalArgumentException("구분자가 없습니다: " + decoded);
            }
            return new ArticleCursor(
                    Instant.ofEpochMilli(Long.parseLong(decoded.substring(0, separator))),
                    Long.parseLong(decoded.substring(separator + 1)));
        } catch (IllegalArgumentException e) {
            throw new InvalidCursorException(encoded, e);
        }
    }

    public String encode() {
        String plain = publishedAt.toEpochMilli() + SEPARATOR + id;
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(plain.getBytes(StandardCharsets.UTF_8));
    }
}
