package com.devhub.support.domain;

import java.util.List;
import java.util.function.Function;

public record Page<T>(List<T> items, String nextCursor) {

    /**
     * @param found    size + 1건으로 조회한 결과. size를 넘으면 다음 페이지가 있다
     * @param size     페이지 크기
     * @param cursorOf 마지막 항목을 다음 커서로 인코딩한다
     */
    public static <T> Page<T> of(List<T> found, int size, Function<T, String> cursorOf) {
        if (found.size() <= size) {
            return new Page<>(List.copyOf(found), null);
        }
        List<T> items = List.copyOf(found.subList(0, size));
        return new Page<>(items, cursorOf.apply(items.getLast()));
    }
}