package com.devhub.article.app;

import com.devhub.article.app.port.out.ArticlePagePolicy;
import com.devhub.article.app.port.out.ArticleRepository;
import com.devhub.article.domain.Article;
import com.devhub.source.app.SourceFinder;
import com.devhub.source.domain.UnknownSourceException;
import com.devhub.support.domain.InvalidCursorException;
import com.devhub.support.domain.Page;
import com.devhub.support.domain.TimeCursor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleFinder {

    private final ArticleRepository repository;
    private final SourceFinder sourceFinder;
    private final ArticlePagePolicy page;

    /**
     * @param cursor 이전 페이지의 nextCursor. 첫 페이지면 null
     * @param source 이 소스의 기사만 가져온다. 소스를 가리지 않으면 null
     * @param limit  한 페이지 크기. null이면 기본값, 범위를 벗어나면 범위 안으로 조정한다.
     * @throws InvalidCursorException 커서를 해석할 수 없으면
     * @throws UnknownSourceException 그 slug를 쓰는 활성 소스가 없으면
     */
    public Page<Article> findPage(String cursor, String source, Integer limit) {
        int pageSize = pageSizeOf(limit);
        List<Article> found = repository.findPage(
                cursor == null ? null : TimeCursor.decode(cursor),
                sourceIdOf(source),
                pageSize + 1);

        return Page.of(found, pageSize, ArticleFinder::cursorOf);
    }

    private static String cursorOf(Article article) {
        return new TimeCursor(article.publishedAt(), article.id()).encode();
    }

    private int pageSizeOf(Integer limit) {
        return limit == null
                ? page.defaultSize()
                : Math.clamp(limit, page.minSize(), page.maxSize());
    }

    private Long sourceIdOf(String slug) {
        if (slug == null) {
            return null;
        }
        return sourceFinder.requireEnabledIdBySlug(slug);
    }
}
