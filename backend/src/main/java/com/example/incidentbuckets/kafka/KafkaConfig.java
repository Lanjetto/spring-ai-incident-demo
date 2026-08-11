package com.example.incidentbuckets.kafka;

import com.example.incidentbuckets.config.IncidentBucketsProperties;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic userMessagesTopic(IncidentBucketsProperties properties) {
        return new NewTopic(properties.getTopic(), 1, (short) 1);
    }
}
