package com.devhub.support;

import com.devhub.collect.CollectProperties;
import java.time.Duration;
import org.springframework.util.unit.DataSize;

public final class CollectPropertiesFixture {

    private static final CollectProperties.Http HTTP = new CollectProperties.Http(
            Duration.ofSeconds(5), Duration.ofSeconds(10), DataSize.ofMegabytes(10), 5);

    private CollectPropertiesFixture() {
    }

    public static CollectProperties defaults() {
        return of(8, HTTP);
    }

    public static CollectProperties of(int concurrency) {
        return of(concurrency, HTTP);
    }

    public static CollectProperties of(CollectProperties.Http http) {
        return of(8, http);
    }

    public static CollectProperties of(int concurrency, CollectProperties.Http http) {
        return new CollectProperties(false, Duration.ofMinutes(30), concurrency, http);
    }
}
