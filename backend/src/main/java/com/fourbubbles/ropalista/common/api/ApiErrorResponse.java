package com.fourbubbles.ropalista.common.api;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
        Instant timestamp,
        String code,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
