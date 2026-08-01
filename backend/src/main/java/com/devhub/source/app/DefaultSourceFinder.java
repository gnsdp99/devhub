package com.devhub.source.app;

import com.devhub.source.domain.Source;
import com.devhub.source.domain.UnknownSourceException;
import com.devhub.source.app.port.out.SourceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultSourceFinder implements SourceFinder {

    private final SourceRepository repository;

    @Override
    public List<Source> findEnabled() {
        return repository.findEnabled();
    }

    /**
     * @throws UnknownSourceException 그 slug를 쓰는 활성 소스가 없으면
     */
    @Override
    public long requireEnabledIdBySlug(String slug) {
        return repository.findEnabledIdBySlug(slug)
                .orElseThrow(() -> new UnknownSourceException(slug));
    }
}
