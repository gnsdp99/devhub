package com.devhub.support;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class UrlNormalizer {

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String CANONICAL_SCHEME = "https";
    private static final Map<String, Integer> DEFAULT_PORTS = Map.of("http", 80, "https", 443);
    private static final Set<String> TRACKING_PARAMS = Set.of("fbclid", "gclid");
    private static final String TRACKING_PARAM_PREFIX = "utm_";
    private static final String WWW_PREFIX = "www.";

    private UrlNormalizer() {
    }

    public static byte[] hash(String rawUrl) {
        byte[] canonical = normalize(rawUrl).getBytes(StandardCharsets.UTF_8);
        try {
            return MessageDigest.getInstance(HASH_ALGORITHM).digest(canonical);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(HASH_ALGORITHM + "을 사용할 수 없습니다.", e);
        }
    }

    static String normalize(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new IllegalArgumentException("URL이 비어 있습니다.");
        }

        URI uri = parse(rawUrl.strip());
        String scheme = requireSupportedScheme(uri);

        StringBuilder normalized = new StringBuilder(CANONICAL_SCHEME)
                .append("://")
                .append(normalizeHost(uri));

        int port = uri.getPort();
        if (port != -1 && port != DEFAULT_PORTS.get(scheme)) {
            normalized.append(':').append(port);
        }
        normalized.append(normalizePath(uri.getRawPath()));

        String query = normalizeQuery(uri.getRawQuery());
        if (!query.isEmpty()) {
            normalized.append('?').append(query);
        }
        return normalized.toString();
    }

    private static URI parse(String rawUrl) {
        try {
            return new URI(rawUrl);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URL 형식이 올바르지 않습니다: " + rawUrl, e);
        }
    }

    private static String requireSupportedScheme(URI uri) {
        String scheme = uri.getScheme();
        if (scheme == null || !DEFAULT_PORTS.containsKey(scheme.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("http 또는 https URL이 아닙니다: " + uri);
        }
        return scheme.toLowerCase(Locale.ROOT);
    }

    private static String normalizeHost(URI uri) {
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("host를 해석할 수 없습니다: " + uri);
        }
        host = host.toLowerCase(Locale.ROOT);
        if (host.startsWith(WWW_PREFIX) && host.length() > WWW_PREFIX.length()) {
            host = host.substring(WWW_PREFIX.length());
        }
        return host;
    }

    private static String normalizePath(String rawPath) {
        if (rawPath == null || rawPath.isEmpty() || "/".equals(rawPath)) {
            return "/";
        }
        return rawPath.endsWith("/") ? rawPath.substring(0, rawPath.length() - 1) : rawPath;
    }

    private static String normalizeQuery(String rawQuery) {
        if (rawQuery == null || rawQuery.isEmpty()) {
            return "";
        }

        List<String> kept = new ArrayList<>();
        for (String param : rawQuery.split("&")) {
            if (!param.isEmpty() && !isTracking(keyOf(param))) {
                kept.add(param);
            }
        }
        kept.sort(Comparator.comparing(UrlNormalizer::keyOf));
        return String.join("&", kept);
    }

    private static String keyOf(String param) {
        int separator = param.indexOf('=');
        return separator == -1 ? param : param.substring(0, separator);
    }

    private static boolean isTracking(String key) {
        String lowered = key.toLowerCase(Locale.ROOT);
        return lowered.startsWith(TRACKING_PARAM_PREFIX) || TRACKING_PARAMS.contains(lowered);
    }
}