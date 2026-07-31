package com.devhub.article;

import com.devhub.source.SourceRepository;
import com.devhub.source.UnknownSourceException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleFinder {

    static final int DEFAULT_LIMIT = 20;
    static final int MIN_LIMIT = 1;
    static final int MAX_LIMIT = 50;

    private final ArticleRepository repository;
    private final SourceRepository sourceRepository;

    /**
     * @param cursor 이전 페이지의 nextCursor. 첫 페이지면 null
     * @param source 이 소스의 기사만 가져온다. 소스를 가리지 않으면 null
     * @param limit  한 페이지 크기. null이면 기본값, 범위를 벗어나면 범위 안으로 조정한다.
     * @throws InvalidCursorException 커서를 해석할 수 없으면
     * @throws UnknownSourceException 그 slug를 쓰는 활성 소스가 없으면
     */
    public ArticlePage findPage(String cursor, String source, Integer limit) {
        int pageSize = pageSizeOf(limit);
        List<Article> found = repository.findPage(
                cursor == null ? null : ArticleCursor.decode(cursor),
                sourceIdOf(source),
                pageSize + 1);

        if (found.size() <= pageSize) {
            return new ArticlePage(found, null);
        }
        List<Article> items = List.copyOf(found.subList(0, pageSize));
        return new ArticlePage(items, ArticleCursor.of(items.getLast()).encode());
    }

    private int pageSizeOf(Integer limit) {
        return limit == null ? DEFAULT_LIMIT : Math.clamp(limit, MIN_LIMIT, MAX_LIMIT);
    }

    private Long sourceIdOf(String slug) {
        if (slug == null) {
            return null;
        }
        return sourceRepository.findEnabledIdBySlug(slug)
                .orElseThrow(() -> new UnknownSourceException(slug));
    }
}