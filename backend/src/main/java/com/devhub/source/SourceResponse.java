package com.devhub.source;

public record SourceResponse(String slug, String name, String category, String siteUrl) {

    public static SourceResponse from(Source source) {
        return new SourceResponse(
                source.slug(), source.name(), source.category(), source.siteUrl());
    }
}
