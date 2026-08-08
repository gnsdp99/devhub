package com.devhub.support.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PageUnitTest {

    private static final int SIZE = 3;

    @Test
    @DisplayName("size를 넘게 조회되면 초과분을 버리고 마지막 항목으로 커서를 만든다")
    void dropsTheExtraRowAndEncodesTheLastAsCursor() {
        Page<String> page = Page.of(rows(SIZE + 1), SIZE, row -> "cursor-" + row);

        assertThat(page.items()).containsExactly("1", "2", "3");
        assertThat(page.nextCursor()).isEqualTo("cursor-3");
    }

    @Test
    @DisplayName("size만큼 조회되면 커서를 반환하지 않는다")
    void givesNoCursorWhenTheRowsExactlyFillThePage() {
        Page<String> page = Page.of(rows(SIZE), SIZE, row -> "cursor-" + row);

        assertThat(page.items()).containsExactly("1", "2", "3");
        assertThat(page.nextCursor()).isNull();
    }

    @Test
    @DisplayName("size보다 적게 조회되면 커서를 반환하지 않는다")
    void givesNoCursorWhenTheRowsFallShort() {
        Page<String> page = Page.of(rows(1), SIZE, row -> "cursor-" + row);

        assertThat(page.items()).containsExactly("1");
        assertThat(page.nextCursor()).isNull();
    }

    @Test
    @DisplayName("조회 결과가 없으면 빈 페이지를 반환한다")
    void givesAnEmptyPage() {
        Page<String> page = Page.of(List.of(), SIZE, row -> "cursor-" + row);

        assertThat(page.items()).isEmpty();
        assertThat(page.nextCursor()).isNull();
    }

    private static List<String> rows(int count) {
        return IntStream.rangeClosed(1, count).mapToObj(String::valueOf).toList();
    }
}