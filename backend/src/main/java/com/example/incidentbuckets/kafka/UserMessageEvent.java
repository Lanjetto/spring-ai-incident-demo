package com.example.incidentbuckets.kafka;

import java.time.Instant;
import java.util.UUID;

/**
 * Событие приёма сообщения пользователя, публикуемое в Kafka.
 */
public record UserMessageEvent(UUID messageId, String userId, String text, Instant receivedAt) {

    public static UserMessageEvent of(String userId, String text, Instant receivedAt) {
        return new UserMessageEvent(UUID.randomUUID(), userId, text, receivedAt);
    }
}
