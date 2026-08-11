package com.example.incidentbuckets.web.dto;

import java.util.List;

import com.example.incidentbuckets.domain.KnowledgeDoc;

/**
 * Похожий исторический инцидент из базы знаний, найденный по RAG.
 */
public record SimilarIncidentResponse(Long id, String title, String symptom, String resolution) {

    public static SimilarIncidentResponse from(KnowledgeDoc doc) {
        return new SimilarIncidentResponse(doc.getId(), doc.getTitle(), doc.getSymptom(), doc.getResolution());
    }
}
