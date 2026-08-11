package com.example.incidentbuckets.web.dto;

import java.time.Instant;

/**
 * Запрос на приём сообщения пользователя.
 */
public record UserMessageRequest(String userId, String message, Instant timestamp) {
}
