package com.devhub.support.domain;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

/**
 * 시각 내림차순으로 정렬된 목록의 keyset 커서. 같은 시각이 여럿일 때 id로 가른다.
 */
public record TimeCursor(Instant at, long id) {

    private static final String SEPARATOR = ":";

    /**
     * @throws InvalidCursorException 커서를 해석할 수 없으면
     */
    public static TimeCursor decode(String encoded) {
        try {
            String decoded = new String(
                    Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
            int separator = decoded.indexOf(SEPARATOR);
            if (separator == -1) {
                throw new IllegalArgumentException("구분자가 없습니다: " + decoded);
            }
            return new TimeCursor(
                    Instant.ofEpochMilli(Long.parseLong(decoded.substring(0, separator))),
                    Long.parseLong(decoded.substring(separator + 1)));
        } catch (IllegalArgumentException e) {
            throw new InvalidCursorException(encoded, e);
        }
    }

    public String encode() {
        String plain = at.toEpochMilli() + SEPARATOR + id;
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(plain.getBytes(StandardCharsets.UTF_8));
    }
}
