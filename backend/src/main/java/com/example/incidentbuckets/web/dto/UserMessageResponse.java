package com.example.incidentbuckets.web.dto;

import com.example.incidentbuckets.domain.Incident;
import com.example.incidentbuckets.domain.UserMessage;

import java.time.Instant;
import java.util.UUID;

public record UserMessageResponse(UUID id, String userId, String text, Instant receivedAt) {

    public static UserMessageResponse from(UserMessage m) {
        return new UserMessageResponse(m.getId(), m.getUserId(), m.getText(), m.getReceivedAt());
    }
}
