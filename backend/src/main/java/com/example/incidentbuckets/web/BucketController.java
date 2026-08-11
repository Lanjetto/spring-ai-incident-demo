package com.example.incidentbuckets.web;

import com.example.incidentbuckets.domain.SeverityLevel;
import com.example.incidentbuckets.repository.IncidentRepository;
import com.example.incidentbuckets.web.dto.BucketResponse;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/buckets")
public class BucketController {

    private final IncidentRepository incidentRepository;

    public BucketController(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    /**
     * Всегда ровно четыре бакета (A1-A4) с агрегатами по открытым инцидентам.
     */
    @GetMapping
    public List<BucketResponse> buckets() {
        return Arrays.stream(SeverityLevel.values())
                .map(level -> new BucketResponse(
                        level,
                        level.getLabel(),
                        level.getDescription(),
                        incidentRepository.countOpenByLevel(level),
                        incidentRepository.sumOpenMessageCountByLevel(level)))
                .toList();
    }
}
