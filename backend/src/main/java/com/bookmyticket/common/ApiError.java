package com.bookmyticket.common;

import java.time.Instant;
import java.util.Map;

/** Consistent JSON error payload for every API failure. */
public record ApiError(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
    public static ApiError of(int status, String code, String message, String path) {
        return new ApiError(Instant.now(), status, code, message, path, Map.of());
    }

    public static ApiError of(int status, String code, String message, String path, Map<String, String> fieldErrors) {
        return new ApiError(Instant.now(), status, code, message, path, fieldErrors);
    }
}
