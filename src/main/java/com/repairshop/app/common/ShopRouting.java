package com.repairshop.app.common;

import java.util.Optional;
import java.util.Set;

public final class ShopRouting {

    private static final Set<String> RESERVED_SEGMENTS = Set.of(
            "shops",
            "track",
            "css",
            "js",
            "images",
            "webjars",
            "error",
            "favicon.ico",
            "actuator"
    );

    private ShopRouting() {
    }

    public static boolean isReservedSlug(String value) {
        return value == null || value.isBlank() || RESERVED_SEGMENTS.contains(value);
    }

    public static Optional<String> extractShopSlug(String requestUri) {
        if (requestUri == null || requestUri.isBlank() || "/".equals(requestUri)) {
            return Optional.empty();
        }

        String path = requestUri.startsWith("/") ? requestUri.substring(1) : requestUri;
        int nextSlash = path.indexOf('/');
        String firstSegment = nextSlash >= 0 ? path.substring(0, nextSlash) : path;

        if (isReservedSlug(firstSegment)) {
            return Optional.empty();
        }

        return Optional.of(firstSegment);
    }
}

