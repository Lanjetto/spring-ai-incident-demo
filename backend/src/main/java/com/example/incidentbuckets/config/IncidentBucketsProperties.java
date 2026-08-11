package com.example.incidentbuckets.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "incident-buckets")
public class IncidentBucketsProperties {

    /** Kafka-топик для приёма сообщений. */
    private String topic = "user-messages";

    /** Через сколько сообщений в инциденте уровень повышается на одну ступень к A1. */
    private int escalationStepMessages = 10;

    /** Сколько последних инцидентов-кандидатов показывать LLM при отнесении. */
    private int assignmentCandidates = 10;

    /** Сколько похожих исторических инцидентов из KB возвращать для подсказки. */
    private int ragTopK = 4;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getEscalationStepMessages() {
        return escalationStepMessages;
    }

    public void setEscalationStepMessages(int escalationStepMessages) {
        this.escalationStepMessages = escalationStepMessages;
    }

    public int getAssignmentCandidates() {
        return assignmentCandidates;
    }

    public void setAssignmentCandidates(int assignmentCandidates) {
        this.assignmentCandidates = assignmentCandidates;
    }

    public int getRagTopK() {
        return ragTopK;
    }

    public void setRagTopK(int ragTopK) {
        this.ragTopK = ragTopK;
    }
}
