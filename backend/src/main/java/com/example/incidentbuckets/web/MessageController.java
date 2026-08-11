package com.example.incidentbuckets.web;

import com.example.incidentbuckets.domain.RawMessage;
import com.example.incidentbuckets.repository.RawMessageRepository;
import com.example.incidentbuckets.web.dto.RawMessageResponse;
import com.example.incidentbuckets.web.dto.UserMessageRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final RawMessageRepository rawMessageRepository;

    public MessageController(RawMessageRepository rawMessageRepository) {
        this.rawMessageRepository = rawMessageRepository;
    }

    /**
     * Принимает сообщение пользователя и сохраняет его как сырое сообщение.
     * Сообщение ждёт ручной кластеризации оператором в инцидент.
     */
    @PostMapping
    public ResponseEntity<UUID> accept(@RequestBody UserMessageRequest request) {
        String userId = request.userId() == null ? "anonymous" : request.userId();
        String text = request.message() == null ? "" : request.message().trim();
        Instant timestamp = request.timestamp() == null ? Instant.now() : request.timestamp();

        RawMessage saved = rawMessageRepository.save(new RawMessage(userId, text, timestamp));
        return ResponseEntity.accepted().body(saved.getId());
    }

    /**
     * Все сырые сообщения, ещё не отнесённые к инцидентам.
     */
    @GetMapping
    public List<RawMessageResponse> rawMessages() {
        return rawMessageRepository.findAllByOrderByTimestampAsc().stream()
                .map(RawMessageResponse::from)
                .toList();
    }
}
