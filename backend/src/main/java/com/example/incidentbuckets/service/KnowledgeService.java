package com.example.incidentbuckets.service;

import com.example.incidentbuckets.domain.Incident;
import com.example.incidentbuckets.domain.KnowledgeDoc;
import com.example.incidentbuckets.domain.UserMessage;
import com.example.incidentbuckets.repository.KnowledgeDocRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Пополнение базы знаний закрытыми инцидентами: сохраняет KnowledgeDoc и
 * векторную запись в pgvector, чтобы решения операторов участвовали в RAG.
 */
@Service
public class KnowledgeService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeService.class);

    private final KnowledgeDocRepository knowledgeDocRepository;
    private final VectorStore vectorStore;

    public KnowledgeService(KnowledgeDocRepository knowledgeDocRepository, VectorStore vectorStore) {
        this.knowledgeDocRepository = knowledgeDocRepository;
        this.vectorStore = vectorStore;
    }

    /**
     * Добавляет закрытый инцидент в базу знаний и векторное хранилище.
     */
    @Transactional
    public void saveClosedIncident(Incident incident, List<UserMessage> messages) {
        if (incident.getResolution() == null || incident.getResolution().isBlank()) {
            return;
        }
        String symptom = messages.stream()
                .map(UserMessage::getText)
                .filter(t -> t != null && !t.isBlank())
                .collect(Collectors.joining(" | "));

        KnowledgeDoc doc = new KnowledgeDoc(incident.getTitle(), symptom, incident.getResolution());
        knowledgeDocRepository.save(doc);

        Document vectorDoc = Document.builder()
                .id(UUID.nameUUIDFromBytes(("knowledge:" + doc.getId()).getBytes()).toString())
                .text(symptom)
                .metadata(Map.of(
                        "title", doc.getTitle(),
                        "resolution", doc.getResolution(),
                        "knowledgeDocId", String.valueOf(doc.getId())))
                .build();
        vectorStore.add(List.of(vectorDoc));

        log.info("Закрытый инцидент {} добавлен в базу знаний (id={})", incident.getId(), doc.getId());
    }
}
