package com.devhub.source;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SourceFinder {

    private final SourceRepository repository;

    public List<Source> findEnabled() {
        return repository.findEnabled();
    }

    /**
     * @throws UnknownSourceException 그 slug를 쓰는 활성 소스가 없으면
     */
    public long requireEnabledIdBySlug(String slug) {
        return repository.findEnabledIdBySlug(slug)
                .orElseThrow(() -> new UnknownSourceException(slug));
    }
}
