package com.devhub.article.infra;

import com.devhub.article.app.port.out.ArticlePagePolicy;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "devhub.article.page")
public record ArticlePageProperties(int defaultSize, int minSize, int maxSize)
        implements ArticlePagePolicy {
}