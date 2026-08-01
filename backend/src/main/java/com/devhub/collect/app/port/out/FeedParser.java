package com.devhub.collect.app.port.out;

import com.devhub.collect.domain.FeedParseException;
import com.devhub.collect.domain.ParsedArticle;
import java.util.List;

public interface FeedParser {

    /**
     * @throws FeedParseException 피드를 파싱할 수 없으면
     */
    List<ParsedArticle> parse(byte[] body);
}
