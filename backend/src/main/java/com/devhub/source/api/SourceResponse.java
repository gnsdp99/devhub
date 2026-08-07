package com.devhub.source.api;

import com.devhub.source.domain.Source;
public record SourceResponse(String slug, String name, String siteUrl, String logoUrl) {

    public static SourceResponse from(Source source) {
        return new SourceResponse(
                source.slug(), source.name(), source.siteUrl(), source.logoUrl());
    }
}
