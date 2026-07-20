package ar.com.ropalista.common.api;

import java.time.OffsetDateTime;

public record ApiResponse<T>(boolean success, T data, OffsetDateTime timestamp) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, OffsetDateTime.now());
    }
}
