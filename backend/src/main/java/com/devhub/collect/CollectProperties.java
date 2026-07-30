package com.devhub.collect;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "devhub.collect")
public record CollectProperties(Duration window) {
}