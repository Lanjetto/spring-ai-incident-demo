package com.example.incidentbuckets.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

/**
 * Сырое сообщение пользователя, ожидающее ручной кластеризации оператором в инцидент.
 * Пока сообщение не отнесено, живёт в raw_messages; после выбора переезжает в user_message.
 */
@Entity
@Table(name = "raw_messages")
public class RawMessage {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private Instant timestamp;

    protected RawMessage() {
    }

    public RawMessage(String userId, String text, Instant timestamp) {
        this.userId = userId;
        this.text = text;
        this.timestamp = timestamp;
    }

    public UUID getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getText() {
        return text;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
