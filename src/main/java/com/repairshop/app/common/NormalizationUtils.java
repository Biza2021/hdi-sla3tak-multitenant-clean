package com.repairshop.app.common;

import java.util.Locale;

public final class NormalizationUtils {

    private NormalizationUtils() {
    }

    public static String normalizeSlug(String value) {
        return normalizeLowercase(value);
    }

    public static String normalizeUsername(String value) {
        return normalizeLowercase(value);
    }

    private static String normalizeLowercase(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}

