package com.devhub.collect.infra;

import com.devhub.collect.app.port.out.CollectionPolicy;
import com.devhub.collect.domain.CollectionWindow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PropertyCollectionPolicy implements CollectionPolicy {

    private final FeedCollectionProperties properties;

    @Override
    public CollectionWindow window() {
        return new CollectionWindow(properties.window());
    }
}