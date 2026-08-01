package com.devhub.collect.domain;

public record Feed(long id, String slug, String feedUrl, String etag, String lastModified) {
}