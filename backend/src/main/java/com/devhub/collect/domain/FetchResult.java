package com.devhub.collect.domain;

public sealed interface FetchResult {

    record NotModified() implements FetchResult {
    }

    record Fetched(byte[] body, String etag, String lastModified) implements FetchResult {
    }
}