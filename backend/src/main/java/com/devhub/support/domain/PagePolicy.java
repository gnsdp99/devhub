package com.devhub.support.domain;

public interface PagePolicy {

    int defaultSize();

    int minSize();

    int maxSize();

    /**
     * @param requested 클라이언트가 준 limit. null이면 기본값, 범위를 벗어나면 범위 안으로 조정한다
     */
    default int sizeOf(Integer requested) {
        return requested == null ? defaultSize() : Math.clamp(requested, minSize(), maxSize());
    }
}