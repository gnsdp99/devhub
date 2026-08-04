package com.devhub.collect.infra;

import static com.devhub.collect.infra.MicrometerFeedCollectionReporter.ARTICLE_COUNTER;
import static com.devhub.collect.infra.MicrometerFeedCollectionReporter.FEED_COUNTER;
import static com.devhub.collect.infra.MicrometerFeedCollectionReporter.RUN_TIMER;
import static org.assertj.core.api.Assertions.assertThat;

import com.devhub.collect.app.port.out.FeedCollectionReporter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MicrometerFeedCollectionReporterUnitTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final FeedCollectionReporter reporter = new MicrometerFeedCollectionReporter(registry);

    @Test
    @DisplayName("수집 결과를 피드별로 구분해 센다")
    void countsFeedsByResult() {
        reporter.collected("toss-tech", 3);
        reporter.unchanged("kakao-tech");
        reporter.failed("woowahan");
        reporter.errored("naver-d2");

        assertThat(feedCount("toss-tech", "collected")).isOne();
        assertThat(feedCount("kakao-tech", "unchanged")).isOne();
        assertThat(feedCount("woowahan", "failed")).isOne();
        assertThat(feedCount("naver-d2", "errored")).isOne();
    }

    @Test
    @DisplayName("신규 기사 건수를 피드별로 누적한다")
    void accumulatesStoredArticles() {
        reporter.collected("toss-tech", 3);
        reporter.collected("toss-tech", 2);
        reporter.collected("kakao-tech", 1);

        assertThat(registry.counter(ARTICLE_COUNTER, "feed", "toss-tech").count()).isEqualTo(5);
        assertThat(registry.counter(ARTICLE_COUNTER, "feed", "kakao-tech").count()).isEqualTo(1);
    }

    @Test
    @DisplayName("측정을 닫아야 수집 1회로 기록한다")
    void recordsARunOnlyWhenTimingCloses() {
        FeedCollectionReporter.Timing timing = reporter.started();
        assertThat(registry.timer(RUN_TIMER).count()).isZero();

        timing.close();

        assertThat(registry.timer(RUN_TIMER).count()).isOne();
    }

    private double feedCount(String feed, String result) {
        return registry.counter(FEED_COUNTER, "feed", feed, "result", result).count();
    }
}