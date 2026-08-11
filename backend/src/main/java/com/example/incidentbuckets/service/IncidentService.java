package com.example.incidentbuckets.service;

import com.example.incidentbuckets.ai.AssignmentService;
import com.example.incidentbuckets.ai.ClassificationService;
import com.example.incidentbuckets.ai.TitleService;
import com.example.incidentbuckets.config.IncidentBucketsProperties;
import com.example.incidentbuckets.domain.Incident;
import com.example.incidentbuckets.domain.SeverityLevel;
import com.example.incidentbuckets.domain.UserMessage;
import com.example.incidentbuckets.kafka.UserMessageEvent;
import com.example.incidentbuckets.repository.IncidentRepository;
import com.example.incidentbuckets.repository.UserMessageRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final UserMessageRepository messageRepository;
    private final ClassificationService classificationService;
    private final AssignmentService assignmentService;
    private final TitleService titleService;
    private final IncidentBucketsProperties properties;

    public IncidentService(IncidentRepository incidentRepository, UserMessageRepository messageRepository,
            ClassificationService classificationService, AssignmentService assignmentService,
            TitleService titleService, IncidentBucketsProperties properties) {
        this.incidentRepository = incidentRepository;
        this.messageRepository = messageRepository;
        this.classificationService = classificationService;
        this.assignmentService = assignmentService;
        this.titleService = titleService;
        this.properties = properties;
    }

    /**
     * Полный поток обработки одного сообщения:
     * классификация -> отнесение к инциденту (или создание нового) -> эскалация.
     */
    @Transactional
    public void processMessage(UserMessageEvent event) {
        SeverityLevel level = classificationService.classify(event.text());

        List<Incident> candidates = incidentRepository.findCandidates(
                candidateLevels(level), PageRequest.of(0, properties.getAssignmentCandidates()));

        UUID incidentId = assignmentService.assign(event.text(), candidates);

        Incident incident;
        if (incidentId != null) {
            Optional<Incident> existing = incidentRepository.findById(incidentId);
            incident = existing.orElseGet(() -> createIncident(event.text(), level, event.receivedAt()));
        } else {
            incident = createIncident(event.text(), level, event.receivedAt());
        }

        incident.registerMessage(event.receivedAt());
        incident.applyEscalation(properties.getEscalationStepMessages());
        incidentRepository.save(incident);

        messageRepository.save(new UserMessage(event.userId(), event.text(), event.receivedAt(), incident));

        regenerateTitle(incident);
    }

    private Incident createIncident(String text, SeverityLevel level, java.time.Instant receivedAt) {
        String title = text.length() > 80 ? text.substring(0, 80) : text;
        return new Incident(title, level, receivedAt);
    }

    /**
     * Генерирует ИИ-название инцидента по накопленным сообщениям.
     * Вызывается при создании и при каждом добавлении сообщения, но LLM дёргается
     * только когда названия ещё нет контекста: на 1-м и 2-м сообщении. Дальше
     * название уже осмысленное и перегенерация не нужна.
     */
    private void regenerateTitle(Incident incident) {
        if (incident.getMessageCount() > 2) {
            return;
        }
        List<String> texts = messageRepository.findByIncidentIdOrderByReceivedAtAsc(incident.getId())
                .stream()
                .map(UserMessage::getText)
                .toList();
        incident.setTitle(titleService.generateTitle(texts));
        incidentRepository.save(incident);
    }

    /**
     * Кандидаты для отнесения: инциденты классифицированного уровня и критичнее.
     * Критичнее (ближе к A1) — потому что эскалированные инциденты должны продолжать
     * поглощать однотипные сообщения, вместо создания дубликатов.
     */
    private List<SeverityLevel> candidateLevels(SeverityLevel level) {
        int ordinal = level.ordinal();
        return java.util.Arrays.stream(SeverityLevel.values())
                .filter(l -> l.ordinal() <= ordinal)
                .toList();
    }
}
