package com.devhub.source.app.port.out;

import com.devhub.source.domain.Source;
import java.util.List;
import java.util.Optional;

public interface SourceRepository {

    List<Source> findEnabled();

    /**
     * @return slug를 쓰는 활성 소스의 id. 없으면 빈 값
     */
    Optional<Long> findEnabledIdBySlug(String slug);
}
