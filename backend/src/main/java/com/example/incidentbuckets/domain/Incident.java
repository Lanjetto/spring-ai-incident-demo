package com.example.incidentbuckets.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "incident")
public class Incident {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String title;

    /** Уровень критичности, заданный оператором при создании инцидента. */
    @Enumerated(EnumType.STRING)
    @Column(name = "base_level", nullable = false, length = 2)
    private SeverityLevel baseLevel;

    /** Текущий уровень критичности. Эскалации нет, уровень фиксируется при создании. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 2)
    private SeverityLevel level;

    @Column(name = "message_count", nullable = false)
    private int messageCount;

    /** Описание решения, введённое оператором при закрытии инцидента. */
    @Column(columnDefinition = "text")
    private String resolution;

    /** Момент закрытия инцидента (null = инцидент открыт). */
    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_message_at", nullable = false)
    private Instant lastMessageAt;

    protected Incident() {
    }

    public Incident(String title, SeverityLevel level, Instant createdAt) {
        this.title = title;
        this.baseLevel = level;
        this.level = level;
        this.messageCount = 0;
        this.createdAt = createdAt;
        this.lastMessageAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public SeverityLevel getBaseLevel() {
        return baseLevel;
    }

    public SeverityLevel getLevel() {
        return level;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public String getResolution() {
        return resolution;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastMessageAt() {
        return lastMessageAt;
    }

    public boolean isOpen() {
        return closedAt == null;
    }

    /**
     * Закрывает инцидент с описанием решения.
     */
    public void close(String resolution, Instant closedAt) {
        this.resolution = resolution;
        this.closedAt = closedAt;
    }

    public void registerMessage(Instant receivedAt) {
        this.messageCount++;
        this.lastMessageAt = receivedAt;
    }
}
