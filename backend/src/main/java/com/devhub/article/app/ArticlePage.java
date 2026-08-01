package com.devhub.article.app;

import com.devhub.article.domain.Article;
import java.util.List;

public record ArticlePage(List<Article> items, String nextCursor) {
}
