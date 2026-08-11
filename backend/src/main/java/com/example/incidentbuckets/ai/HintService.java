package com.example.incidentbuckets.ai;

import com.example.incidentbuckets.config.IncidentBucketsProperties;
import com.example.incidentbuckets.domain.Incident;
import com.example.incidentbuckets.domain.KnowledgeDoc;
import com.example.incidentbuckets.domain.UserMessage;
import com.example.incidentbuckets.repository.KnowledgeDocRepository;
import com.example.incidentbuckets.web.dto.HintResponse;
import com.example.incidentbuckets.web.dto.SimilarIncidentResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

/**
 * RAG-подсказка по инциденту: ищет похожие исторические инциденты в KB,
 * возвращает их и просит LLM составить пошаговый план действий по их решениям.
 */
@Service
public class HintService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final KnowledgeDocRepository knowledgeDocRepository;
    private final Resource hintPrompt;
    private final int ragTopK;

    public HintService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore,
            KnowledgeDocRepository knowledgeDocRepository, ResourceLoader resourceLoader,
            IncidentBucketsProperties properties) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
        this.knowledgeDocRepository = knowledgeDocRepository;
        this.hintPrompt = resourceLoader.getResource("classpath:prompts/hint.st");
        this.ragTopK = properties.getRagTopK();
    }

    public HintResponse generate(Incident incident, List<UserMessage> messages) {
        String query = toQuery(messages);

        List<SimilarIncidentResponse> similar = findSimilar(query);
        String steps = buildSteps(query, similar);

        return new HintResponse(splitSteps(steps), similar);
    }

    /**
     * Похожие исторические инциденты по RAG-поиску. Дёшево (без LLM), вызывается и при
     * уже закэшированных шагах, чтобы вернуть ссылки на базу знаний.
     */
    public List<SimilarIncidentResponse> similarIncidents(List<UserMessage> messages) {
        return findSimilar(toQuery(messages));
    }

    private String toQuery(List<UserMessage> messages) {
        return messages.stream()
                .map(UserMessage::getText)
                .collect(Collectors.joining("\n"));
    }

    private List<SimilarIncidentResponse> findSimilar(String query) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(ragTopK)
                .build();
        List<Document> docs = vectorStore.similaritySearch(request);

        List<SimilarIncidentResponse> result = new ArrayList<>();
        var seen = new HashSet<String>();
        for (Document doc : docs) {
            String id = String.valueOf(doc.getMetadata().get("knowledgeDocId"));
            SimilarIncidentResponse response = knowledgeDocRepository.findById(Long.parseLong(id))
                    .map(SimilarIncidentResponse::from)
                    .orElse(null);
            if (response == null) {
                continue;
            }
            String dedupeKey = response.title() + "|" + response.symptom() + "|" + response.resolution();
            if (seen.add(dedupeKey)) {
                result.add(response);
            }
        }
        return result;
    }

    private String buildSteps(String query, List<SimilarIncidentResponse> similar) {
        String similarText = similar.isEmpty()
                ? "(нет данных)"
                : similar.stream()
                        .map(s -> "- " + s.title() + "\n  симптом: " + s.symptom() + "\n  решение: " + s.resolution())
                        .collect(Collectors.joining("\n"));

        return chatClient.prompt()
                .user(prompt -> prompt.text(hintPrompt)
                        .param("messages", query)
                        .param("similar_incidents", similarText))
                .call()
                .content();
    }

    private List<String> splitSteps(String text) {
        if (text == null || text.isBlank()) {
            return List.of("Нет данных в базе знаний");
        }
        return text.lines()
                .map(String::trim)
                .filter(l -> !l.isEmpty())
                .toList();
    }
}
