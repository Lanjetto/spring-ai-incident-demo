package com.example.incidentbuckets.web;

import com.example.incidentbuckets.ai.HintService;
import com.example.incidentbuckets.domain.Incident;
import com.example.incidentbuckets.domain.SeverityLevel;
import com.example.incidentbuckets.domain.UserMessage;
import com.example.incidentbuckets.repository.IncidentRepository;
import com.example.incidentbuckets.repository.UserMessageRepository;
import com.example.incidentbuckets.service.KnowledgeService;
import com.example.incidentbuckets.web.dto.CloseIncidentRequest;
import com.example.incidentbuckets.web.dto.HintResponse;
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
    private final HintService hintService;
    private final KnowledgeService knowledgeService;

    public IncidentController(IncidentRepository incidentRepository, UserMessageRepository messageRepository,
            HintService hintService, KnowledgeService knowledgeService) {
        this.incidentRepository = incidentRepository;
        this.messageRepository = messageRepository;
        this.hintService = hintService;
        this.knowledgeService = knowledgeService;
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
     * Генерация (и кэширование) RAG-подсказки ИИ по инциденту.
     * Похожие инциденты из KB ищем заново при каждом запросе — это дёшево,
     * кэшируются только шаги от LLM.
     */
    @PostMapping("/incidents/{id}/hint")
    @Transactional
    public ResponseEntity<HintResponse> hint(@PathVariable UUID id) {
        Incident incident = incidentRepository.findById(id).orElse(null);
        if (incident == null) {
            return ResponseEntity.notFound().build();
        }
        List<UserMessage> messages = messageRepository.findByIncidentIdOrderByReceivedAtAsc(id);
        if (incident.getHint() == null) {
            HintResponse hint = hintService.generate(incident, messages);
            incident.setHint(hint.steps().stream().collect(java.util.stream.Collectors.joining("\n")));
            incidentRepository.save(incident);
        }
        List<String> steps = incident.getHint().lines().map(String::trim).filter(l -> !l.isEmpty()).toList();
        return ResponseEntity.ok(new HintResponse(steps, hintService.similarIncidents(messages)));
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
        knowledgeService.saveClosedIncident(incident, messages);
        return ResponseEntity.ok(IncidentDetailResponse.from(incident, messages));
    }
}
