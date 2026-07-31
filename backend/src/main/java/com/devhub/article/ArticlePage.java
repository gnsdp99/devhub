package com.devhub.article;

import java.util.List;

public record ArticlePage(List<Article> items, String nextCursor) {
}
