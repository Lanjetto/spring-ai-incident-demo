package com.example.incidentbuckets.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserMessageProducer {

    private final KafkaTemplate<String, UserMessageEvent> kafkaTemplate;
    private final String topic;

    public UserMessageProducer(KafkaTemplate<String, UserMessageEvent> kafkaTemplate,
            @Value("${incident-buckets.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(UserMessageEvent event) {
        kafkaTemplate.send(topic, event.userId(), event);
    }
}
