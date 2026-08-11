package com.example.incidentbuckets;

import com.example.incidentbuckets.config.IncidentBucketsProperties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(IncidentBucketsProperties.class)
public class IncidentBucketsApplication {

    public static void main(String[] args) {
        SpringApplication.run(IncidentBucketsApplication.class, args);
    }
}
