package com.devhub.collect.infra;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "devhub.collect")
public record FeedCollectionProperties(Duration window) {
}