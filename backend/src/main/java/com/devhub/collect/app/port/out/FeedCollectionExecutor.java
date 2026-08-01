package com.devhub.collect.app.port.out;

import com.devhub.collect.domain.Feed;
import java.util.List;
import java.util.function.Consumer;

public interface FeedCollectionExecutor {

    /**
     * 모든 피드에 대해 작업을 실행하고, 전부 끝난 뒤에 돌아온다.
     */
    void runAll(List<Feed> feeds, Consumer<Feed> collectOne);
}