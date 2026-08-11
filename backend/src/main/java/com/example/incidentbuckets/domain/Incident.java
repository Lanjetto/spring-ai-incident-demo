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

    /** Уровень критичности, поставленный классификатором по первому сообщению. */
    @Enumerated(EnumType.STRING)
    @Column(name = "base_level", nullable = false, length = 2)
    private SeverityLevel baseLevel;

    /** Текущий уровень критичности после эскалации. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 2)
    private SeverityLevel level;

    @Column(name = "message_count", nullable = false)
    private int messageCount;

    /** Кэшированная RAG-подсказка ИИ. */
    @Column(columnDefinition = "text")
    private String hint;

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

    public Incident(String title, SeverityLevel baseLevel, Instant createdAt) {
        this.title = title;
        this.baseLevel = baseLevel;
        this.level = baseLevel;
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

    public String getHint() {
        return hint;
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

    public void setTitle(String title) {
        this.title = title;
    }

    public void setHint(String hint) {
        this.hint = hint;
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

    /**
     * Эскалация: чем больше сообщений в инциденте, тем ближе уровень к A1.
     * Каждые escalationStepMessages сообщений поднимают критичность на одну ступень.
     */
    public void applyEscalation(int escalationStepMessages) {
        int steps = messageCount / escalationStepMessages;
        int currentOrdinal = level.ordinal();
        int newOrdinal = Math.max(0, currentOrdinal - steps);
        level = SeverityLevel.values()[newOrdinal];
    }
}
