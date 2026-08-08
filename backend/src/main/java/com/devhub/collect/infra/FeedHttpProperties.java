package com.devhub.collect.infra;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "devhub.collect.http")
public record FeedHttpProperties(
        String userAgent,
        Duration connectTimeout,
        Duration readTimeout,
        DataSize maxBodySize,
        int maxRedirects) {
}