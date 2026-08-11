package com.example.incidentbuckets.web.dto;

import java.time.Instant;

/**
 * Единый формат ошибки в теле ответа.
 */
public record ApiError(int status, String error, String message, Instant timestamp) {

    public static ApiError of(int status, String error, String message) {
        return new ApiError(status, error, message, Instant.now());
    }
}
