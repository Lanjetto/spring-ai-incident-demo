package com.example.incidentbuckets.web;

import com.example.incidentbuckets.kafka.UserMessageEvent;
import com.example.incidentbuckets.kafka.UserMessageProducer;
import com.example.incidentbuckets.web.dto.UserMessageRequest;

import java.time.Instant;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final UserMessageProducer producer;

    public MessageController(UserMessageProducer producer) {
        this.producer = producer;
    }

    /**
     * Принимает сообщение пользователя и публикует его в Kafka для асинхронной обработки.
     */
    @PostMapping
    public ResponseEntity<UUID> accept(@RequestBody UserMessageRequest request) {
        String userId = request.userId() == null ? "anonymous" : request.userId();
        String text = request.message() == null ? "" : request.message().trim();
        Instant receivedAt = request.timestamp() == null ? Instant.now() : request.timestamp();

        UserMessageEvent event = UserMessageEvent.of(userId, text, receivedAt);
        producer.publish(event);
        return ResponseEntity.accepted().body(event.messageId());
    }
}
