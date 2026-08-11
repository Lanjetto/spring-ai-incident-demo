package com.example.incidentbuckets.web.dto;

import com.example.incidentbuckets.domain.SeverityLevel;

import java.util.List;
import java.util.UUID;

/**
 * Запрос на ручное создание инцидента из выбранных сырых сообщений.
 */
public record CreateIncidentRequest(String title, SeverityLevel level, List<UUID> messageIds) {
}
