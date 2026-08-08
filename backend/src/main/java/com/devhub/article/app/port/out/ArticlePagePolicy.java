package com.devhub.article.app.port.out;

public interface ArticlePagePolicy {

    int defaultSize();

    int minSize();

    int maxSize();
}