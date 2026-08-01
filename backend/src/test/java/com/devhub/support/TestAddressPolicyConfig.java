package com.devhub.support;

import com.devhub.collect.domain.FeedAddressPolicy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * 스텁 서버가 127.0.0.1에 뜨므로 통합 테스트에서는 주소 검사를 통과시킨다.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestAddressPolicyConfig {

    public static final FeedAddressPolicy ALLOW_ALL = uri -> {
    };

    @Bean
    @Primary
    FeedAddressPolicy stubServerAddressPolicy() {
        return ALLOW_ALL;
    }
}