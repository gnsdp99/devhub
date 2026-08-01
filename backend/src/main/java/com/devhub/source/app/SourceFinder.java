package com.devhub.source.app;

import com.devhub.source.domain.Source;
import com.devhub.source.domain.UnknownSourceException;
import java.util.List;

public interface SourceFinder {

    List<Source> findEnabled();

    /**
     * @throws UnknownSourceException 그 slug를 쓰는 활성 소스가 없으면
     */
    long requireEnabledIdBySlug(String slug);
}
