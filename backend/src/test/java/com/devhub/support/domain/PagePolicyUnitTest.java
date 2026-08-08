package com.devhub.support.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PagePolicyUnitTest {

    private final PagePolicy policy = new FixedPagePolicy(20, 5, 50);

    @Test
    @DisplayName("limit이 없으면 기본 크기를 쓴다")
    void fallsBackToTheDefaultSize() {
        assertThat(policy.sizeOf(null)).isEqualTo(20);
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({"5, 5", "30, 30", "50, 50"})
    @DisplayName("범위 안이면 요청한 크기를 그대로 쓴다")
    void keepsASizeWithinRange(int requested, int expected) {
        assertThat(policy.sizeOf(requested)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({"51, 50", "1000, 50", "4, 5", "0, 5", "-1, 5"})
    @DisplayName("범위를 벗어나면 범위 안으로 조정한다")
    void clampsASizeOutOfRange(int requested, int expected) {
        assertThat(policy.sizeOf(requested)).isEqualTo(expected);
    }

    private record FixedPagePolicy(int defaultSize, int minSize, int maxSize)
            implements PagePolicy {
    }
}