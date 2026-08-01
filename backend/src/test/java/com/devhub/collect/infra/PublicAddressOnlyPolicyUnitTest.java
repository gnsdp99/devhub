package com.devhub.collect.infra;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devhub.collect.domain.FeedFetchException;
import java.net.URI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PublicAddressOnlyPolicyUnitTest {

    private final PublicAddressOnlyPolicy policy = new PublicAddressOnlyPolicy();

    @Nested
    @DisplayName("막는 주소")
    class Blocked {

        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = {
                "http://169.254.169.254/latest/meta-data/",
                "http://[fd00::1]/feed.xml",
                "http://127.0.0.1:8080/feed.xml",
                "http://localhost/feed.xml",
                "http://[::1]/feed.xml",
                "http://10.0.0.5/feed.xml",
                "http://192.168.0.1/feed.xml",
                "http://172.16.0.1/feed.xml",
                "http://0.0.0.0/feed.xml",
        })
        @DisplayName("클라우드 메타데이터와 사내망으로는 요청하지 않는다")
        void rejectsInternalAddresses(String url) {
            assertThatThrownBy(() -> policy.check(URI.create(url)))
                    .isInstanceOf(FeedFetchException.class);
        }

        @Test
        @DisplayName("host가 없으면 거부한다")
        void rejectsAUriWithoutHost() {
            assertThatThrownBy(() -> policy.check(URI.create("file:///etc/passwd")))
                    .isInstanceOf(FeedFetchException.class);
        }

        @Test
        @DisplayName("올바르지 않은 host는 거부한다")
        void rejectsAnUnresolvableHost() {
            assertThatThrownBy(() -> policy.check(URI.create("https://no-such-host.invalid/feed")))
                    .isInstanceOf(FeedFetchException.class);
        }
    }

    @Nested
    @DisplayName("허용하는 주소")
    class Allowed {

        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = {
                "https://8.8.8.8/feed.xml",
                "https://[2001:4860:4860::8888]/feed.xml",
        })
        @DisplayName("공인 주소는 통과시킨다")
        void allowsPublicAddresses(String url) {
            assertThatCode(() -> policy.check(URI.create(url))).doesNotThrowAnyException();
        }
    }
}