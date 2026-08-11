package com.example.incidentbuckets.web;

import com.example.incidentbuckets.domain.Incident;
import com.example.incidentbuckets.domain.SeverityLevel;
import com.example.incidentbuckets.domain.UserMessage;
import com.example.incidentbuckets.repository.IncidentRepository;
import com.example.incidentbuckets.repository.UserMessageRepository;
import com.example.incidentbuckets.service.IncidentService;
import com.example.incidentbuckets.web.dto.CloseIncidentRequest;
import com.example.incidentbuckets.web.dto.CreateIncidentRequest;
import com.example.incidentbuckets.web.dto.IncidentDetailResponse;
import com.example.incidentbuckets.web.dto.IncidentResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class IncidentController {

    private final IncidentRepository incidentRepository;
    private final UserMessageRepository messageRepository;
    private final IncidentService incidentService;

    public IncidentController(IncidentRepository incidentRepository, UserMessageRepository messageRepository,
            IncidentService incidentService) {
        this.incidentRepository = incidentRepository;
        this.messageRepository = messageRepository;
        this.incidentService = incidentService;
    }

    /**
     * Открытые инциденты в бакете уровня критичности.
     */
    @GetMapping("/buckets/{level}/incidents")
    public List<IncidentResponse> incidentsInBucket(@PathVariable SeverityLevel level) {
        return incidentRepository.findOpenByLevelOrderByLastMessageAtDesc(level)
                .stream()
                .map(IncidentResponse::from)
                .toList();
    }

    /**
     * Инцидент со всеми сообщениями пользователей.
     */
    @GetMapping("/incidents/{id}")
    public ResponseEntity<IncidentDetailResponse> incident(@PathVariable UUID id) {
        return incidentRepository.findById(id)
                .map(incident -> {
                    List<UserMessage> messages = messageRepository.findByIncidentIdOrderByReceivedAtAsc(id);
                    return ResponseEntity.ok(IncidentDetailResponse.from(incident, messages));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Ручное создание инцидента оператором из выбранных сырых сообщений.
     */
    @PostMapping("/incidents")
    @Transactional
    public ResponseEntity<IncidentDetailResponse> create(@RequestBody CreateIncidentRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new IllegalArgumentException("Название инцидента не может быть пустым");
        }
        Incident incident = incidentService.createIncident(request.title().trim(), request.level(),
                request.messageIds());
        List<UserMessage> messages = messageRepository.findByIncidentIdOrderByReceivedAtAsc(incident.getId());
        return ResponseEntity.ok(IncidentDetailResponse.from(incident, messages));
    }

    /**
     * Закрытие инцидента с описанием решения.
     */
    @PostMapping("/incidents/{id}/close")
    @Transactional
    public ResponseEntity<IncidentDetailResponse> close(@PathVariable UUID id,
            @RequestBody CloseIncidentRequest request) {
        Incident incident = incidentRepository.findById(id).orElse(null);
        if (incident == null) {
            return ResponseEntity.notFound().build();
        }
        incident.close(request.resolution(), Instant.now());
        incidentRepository.save(incident);
        List<UserMessage> messages = messageRepository.findByIncidentIdOrderByReceivedAtAsc(id);
        return ResponseEntity.ok(IncidentDetailResponse.from(incident, messages));
    }
}
