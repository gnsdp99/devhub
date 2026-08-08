package com.devhub.collect.infra;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "devhub.collect.execution")
public record FeedExecutionProperties(int concurrency, int hostConcurrency) {
}