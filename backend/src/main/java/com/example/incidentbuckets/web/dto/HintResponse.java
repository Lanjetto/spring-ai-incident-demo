package com.example.incidentbuckets.web.dto;

import java.util.List;

/**
 * Структурированная подсказка ИИ по инциденту:
 * пошаговый план действий + конкретные похожие инциденты из базы знаний.
 */
public record HintResponse(List<String> steps, List<SimilarIncidentResponse> similarIncidents) {
}
