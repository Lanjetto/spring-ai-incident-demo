package com.example.incidentbuckets.web.dto;

import com.example.incidentbuckets.domain.Incident;
import com.example.incidentbuckets.domain.SeverityLevel;
import com.example.incidentbuckets.domain.UserMessage;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record IncidentDetailResponse(UUID id, String title, SeverityLevel level, SeverityLevel baseLevel,
        int messageCount, Instant createdAt, Instant lastMessageAt, boolean open, String hint, String resolution,
        Instant closedAt, List<UserMessageResponse> messages) {

    public static IncidentDetailResponse from(Incident incident, List<UserMessage> messages) {
        return new IncidentDetailResponse(incident.getId(), incident.getTitle(), incident.getLevel(),
                incident.getBaseLevel(), incident.getMessageCount(), incident.getCreatedAt(),
                incident.getLastMessageAt(), incident.isOpen(), incident.getHint(), incident.getResolution(),
                incident.getClosedAt(), messages.stream().map(UserMessageResponse::from).toList());
    }
}
