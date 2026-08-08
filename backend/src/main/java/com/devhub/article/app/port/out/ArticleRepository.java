package com.devhub.article.app.port.out;

import com.devhub.article.domain.Article;
import com.devhub.support.domain.TimeCursor;
import com.devhub.article.domain.NewArticle;
import java.util.List;

public interface ArticleRepository {

    /**
     * @param cursor   이 커서보다 오래된 기사만 가져온다. 첫 페이지면 null
     * @param sourceId 이 소스의 기사만 가져온다. 소스를 가리지 않으면 null
     */
    List<Article> findPage(TimeCursor cursor, Long sourceId, int limit);

    /**
     * @return 새로 저장한 건수. 그 소스가 이미 가지고 있던 기사는 세지 않는다.
     */
    int insertNew(List<NewArticle> articles);
}
