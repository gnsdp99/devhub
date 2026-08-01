package com.devhub.article.app;

import com.devhub.article.domain.NewArticle;
import java.util.List;

public interface ArticleWriter {

    /**
     * @return 새로 저장한 건수. 그 소스가 이미 가지고 있던 기사는 세지 않는다.
     */
    int write(List<NewArticle> articles);
}
