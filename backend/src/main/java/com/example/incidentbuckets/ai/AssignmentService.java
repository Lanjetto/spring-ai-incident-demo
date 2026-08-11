package com.example.incidentbuckets.ai;

import com.example.incidentbuckets.domain.Incident;

import java.util.List;
import java.util.UUID;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

/**
 * Решение LLM: отнести сообщение к существующему инциденту или создать новый.
 */
@Service
public class AssignmentService {

    private final ChatClient chatClient;
    private final Resource assignPrompt;

    public AssignmentService(ChatClient.Builder chatClientBuilder, ResourceLoader resourceLoader) {
        this.chatClient = chatClientBuilder.build();
        this.assignPrompt = resourceLoader.getResource("classpath:prompts/assign.st");
    }

    /**
     * Возвращает UUID существующего инцидента, к которому относится сообщение,
     * либо {@code null}, если это новая проблема.
     */
    public UUID assign(String message, List<Incident> candidates) {
        StringBuilder sb = new StringBuilder();
        if (candidates.isEmpty()) {
            sb.append("(нет текущих открытых инцидентов)");
        } else {
            for (Incident i : candidates) {
                sb.append(i.getId()).append(": ").append(i.getTitle())
                        .append(" [").append(i.getLevel()).append(", ").append(i.getMessageCount()).append("]\n");
            }
        }

        try {
            String answer = chatClient.prompt()
                    .user(prompt -> prompt.text(assignPrompt)
                            .param("incidents", sb.toString().trim())
                            .param("message", message))
                    .call()
                    .content();
            return parseIncidentId(answer);
        } catch (Exception e) {
            return null;
        }
    }

    private UUID parseIncidentId(String answer) {
        if (answer == null || answer.trim().toUpperCase().startsWith("NEW")) {
            return null;
        }
        String candidate = answer.trim().split("\\s+")[0];
        try {
            return UUID.fromString(candidate);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
