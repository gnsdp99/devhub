package com.devhub.collect;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "devhub.collect")
public record CollectProperties(
        boolean enabled,
        Duration interval,
        int concurrency,
        Http http) {

    public record Http(
            Duration connectTimeout,
            Duration readTimeout,
            DataSize maxBodySize,
            int maxRedirects) {
    }
}