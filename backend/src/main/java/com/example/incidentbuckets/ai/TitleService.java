package com.example.incidentbuckets.ai;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

/**
 * Генерация короткого названия инцидента по набору сообщений пользователей.
 */
@Service
public class TitleService {

    private static final int MAX_TITLE_LENGTH = 120;

    private final ChatClient chatClient;
    private final Resource titlePrompt;

    public TitleService(ChatClient.Builder chatClientBuilder, ResourceLoader resourceLoader) {
        this.chatClient = chatClientBuilder.build();
        this.titlePrompt = resourceLoader.getResource("classpath:prompts/title.st");
    }

    /**
     * Генерирует название по сообщениям. При ошибке LLM возвращает первое сообщение.
     */
    public String generateTitle(List<String> messages) {
        String messageText = messages.stream()
                .map(String::trim)
                .filter(l -> !l.isEmpty())
                .collect(Collectors.joining("\n"));

        try {
            String answer = chatClient.prompt()
                    .user(prompt -> prompt.text(titlePrompt).param("messages", messageText))
                    .call()
                    .content();
            String title = answer == null ? "" : answer.trim();
            if (!title.isEmpty() && title.length() <= MAX_TITLE_LENGTH) {
                return title;
            }
        } catch (Exception e) {
            // fall back to first message below
        }

        String fallback = messages.stream().findFirst().orElse("Новый инцидент");
        return fallback.length() > 80 ? fallback.substring(0, 80) : fallback;
    }
}
