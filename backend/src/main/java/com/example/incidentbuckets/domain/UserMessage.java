package com.example.incidentbuckets.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "user_message")
public class UserMessage {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String text;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    protected UserMessage() {
    }

    public UserMessage(String userId, String text, Instant receivedAt, Incident incident) {
        this.userId = userId;
        this.text = text;
        this.receivedAt = receivedAt;
        this.incident = incident;
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

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public Incident getIncident() {
        return incident;
    }
}
