package com.devhub.collect;

public record Feed(long id, String slug, String feedUrl, String etag, String lastModified) {
}