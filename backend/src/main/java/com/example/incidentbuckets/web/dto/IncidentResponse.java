package com.example.incidentbuckets.web.dto;

import com.example.incidentbuckets.domain.Incident;
import com.example.incidentbuckets.domain.SeverityLevel;

import java.time.Instant;
import java.util.UUID;

public record IncidentResponse(UUID id, String title, SeverityLevel level, SeverityLevel baseLevel,
        int messageCount, Instant createdAt, Instant lastMessageAt, boolean open) {

    public static IncidentResponse from(Incident incident) {
        return new IncidentResponse(incident.getId(), incident.getTitle(), incident.getLevel(),
                incident.getBaseLevel(), incident.getMessageCount(), incident.getCreatedAt(),
                incident.getLastMessageAt(), incident.isOpen());
    }
}
