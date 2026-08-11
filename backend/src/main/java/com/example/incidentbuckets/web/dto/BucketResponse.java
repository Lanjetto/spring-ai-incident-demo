package com.example.incidentbuckets.web.dto;

import com.example.incidentbuckets.domain.SeverityLevel;

public record BucketResponse(SeverityLevel level, String label, String description, long incidentCount,
        long messageCount) {
}
