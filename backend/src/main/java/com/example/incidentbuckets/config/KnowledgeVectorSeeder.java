package com.example.incidentbuckets.config;

import com.example.incidentbuckets.domain.KnowledgeDoc;
import com.example.incidentbuckets.repository.KnowledgeDocRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Загружает сидированные исторические инциденты в pgvector (эмбеддинги через ollama).
 * Выполняется один раз при старте, если векторное хранилище пусто.
 */
@Component
public class KnowledgeVectorSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeVectorSeeder.class);

    private final KnowledgeDocRepository knowledgeDocRepository;
    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;

    public KnowledgeVectorSeeder(KnowledgeDocRepository knowledgeDocRepository, VectorStore vectorStore,
            JdbcTemplate jdbcTemplate) {
        this.knowledgeDocRepository = knowledgeDocRepository;
        this.vectorStore = vectorStore;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        Long count = jdbcTemplate.queryForObject("select count(*) from vector_store", Long.class);
        if (count != null && count > 0) {
            log.info("pgvector уже наполнен ({} документов), сидирование пропущено", count);
            return;
        }

        List<KnowledgeDoc> docs = knowledgeDocRepository.findAllByOrderByIdAsc();
        log.info("Сидирую {} исторических инцидентов в pgvector...", docs.size());

        List<Document> documents = docs.stream()
                .map(d -> Document.builder()
                        .id(UUID.nameUUIDFromBytes(("knowledge:" + d.getId()).getBytes()).toString())
                        .text(d.getSymptom())
                        .metadata(Map.of(
                                "title", d.getTitle(),
                                "resolution", d.getResolution(),
                                "knowledgeDocId", String.valueOf(d.getId())))
                        .build())
                .toList();

        vectorStore.add(documents);
        log.info("pgvector наполнен {} документами", documents.size());
    }
}
