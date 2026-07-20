package ar.com.ropalista.common.api;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiErrorResponse(
        boolean success,
        String code,
        String message,
        int status,
        String path,
        OffsetDateTime timestamp,
        List<FieldViolation> violations
) {
    public static ApiErrorResponse of(String code, String message, int status, String path) {
        return new ApiErrorResponse(false, code, message, status, path, OffsetDateTime.now(), List.of());
    }

    public record FieldViolation(String field, String message, Object rejectedValue) {}
}
