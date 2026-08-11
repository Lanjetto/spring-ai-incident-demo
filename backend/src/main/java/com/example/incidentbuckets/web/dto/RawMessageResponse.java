package com.example.incidentbuckets.web.dto;

import com.example.incidentbuckets.domain.RawMessage;

import java.time.Instant;
import java.util.UUID;

/**
 * Сырое сообщение пользователя, ожидающее ручной кластеризации.
 */
public record RawMessageResponse(UUID id, String userId, String text, Instant timestamp) {

    public static RawMessageResponse from(RawMessage m) {
        return new RawMessageResponse(m.getId(), m.getUserId(), m.getText(), m.getTimestamp());
    }
}
