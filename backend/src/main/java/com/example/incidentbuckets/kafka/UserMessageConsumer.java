package com.example.incidentbuckets.kafka;

import com.example.incidentbuckets.service.IncidentService;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserMessageConsumer {

    private final IncidentService incidentService;

    public UserMessageConsumer(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @KafkaListener(topics = "${incident-buckets.topic}")
    public void onMessage(UserMessageEvent event) {
        incidentService.processMessage(event);
    }
}
