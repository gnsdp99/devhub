package com.devhub.article.app;

import com.devhub.article.app.port.out.ArticleRepository;
import com.devhub.article.domain.NewArticle;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultArticleWriter implements ArticleWriter {

    private final ArticleRepository repository;

    @Override
    public int write(List<NewArticle> articles) {
        return repository.insertNew(articles);
    }
}
