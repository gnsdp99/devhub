package com.devhub.support.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class TimeCursorUnitTest {

    private static final Instant AT = Instant.parse("2026-07-20T12:00:00.123Z");

    @Test
    @DisplayName("인코딩했다가 디코딩하면 시각과 id가 그대로 남는다")
    void keepsTimeAndIdThroughEncodingAndDecoding() {
        TimeCursor cursor = new TimeCursor(AT, 42);

        assertThat(TimeCursor.decode(cursor.encode())).isEqualTo(cursor);
    }

    @Test
    @DisplayName("URL에 그대로 넣을 수 있는 문자만 쓴다")
    void usesOnlyUrlSafeCharacters() {
        String encoded = new TimeCursor(AT, 42).encode();

        assertThat(encoded).matches("[A-Za-z0-9_-]+");
    }

    @Nested
    @DisplayName("해석할 수 없는 커서")
    class Undecodable {

        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = {"", "!!!", "커서"})
        @DisplayName("base64가 아니면 InvalidCursorException을 던진다")
        void rejectsCursorsThatAreNotBase64(String encoded) {
            assertThatThrownBy(() -> TimeCursor.decode(encoded))
                    .isInstanceOf(InvalidCursorException.class);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource
        @DisplayName("시각과 id로 읽을 수 없으면 InvalidCursorException을 던진다")
        void rejectsCursorsThatAreNotATimeAndId(String plain) {
            String encoded = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(plain.getBytes(StandardCharsets.UTF_8));

            assertThatThrownBy(() -> TimeCursor.decode(encoded))
                    .isInstanceOf(InvalidCursorException.class);
        }

        static String[] rejectsCursorsThatAreNotATimeAndId() {
            return new String[] {"", "1784000000000", "1784000000000:", "hello:42", "1:2:3"};
        }
    }
}