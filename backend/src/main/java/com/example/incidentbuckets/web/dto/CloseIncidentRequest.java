package com.example.incidentbuckets.web.dto;

/**
 * Запрос на закрытие инцидента с описанием решения.
 */
public record CloseIncidentRequest(String resolution) {
}
