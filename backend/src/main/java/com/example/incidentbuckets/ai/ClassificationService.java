package com.example.incidentbuckets.ai;

import com.example.incidentbuckets.domain.SeverityLevel;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

/**
 * Определение уровня критичности сообщения LLM по тексту.
 */
@Service
public class ClassificationService {

    private final ChatClient chatClient;
    private final Resource classifyPrompt;

    public ClassificationService(ChatClient.Builder chatClientBuilder, ResourceLoader resourceLoader) {
        this.chatClient = chatClientBuilder.build();
        this.classifyPrompt = resourceLoader.getResource("classpath:prompts/classify.st");
    }

    /**
     * Классифицирует сообщение и возвращает уровень критичности.
     * Нераспознанный ответ модели трактуется как A4.
     */
    public SeverityLevel classify(String message) {
        try {
            String answer = chatClient.prompt()
                    .user(prompt -> prompt.text(classifyPrompt).param("message", message))
                    .call()
                    .content();
            return parseLevel(answer);
        } catch (Exception e) {
            return SeverityLevel.A4;
        }
    }

    private SeverityLevel parseLevel(String answer) {
        if (answer == null) {
            return SeverityLevel.A4;
        }
        for (SeverityLevel level : SeverityLevel.values()) {
            if (answer.trim().toUpperCase().startsWith(level.name())) {
                return level;
            }
        }
        return SeverityLevel.A4;
    }
}
