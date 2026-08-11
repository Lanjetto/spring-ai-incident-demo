package com.example.incidentbuckets.service;

import com.example.incidentbuckets.domain.Incident;
import com.example.incidentbuckets.domain.RawMessage;
import com.example.incidentbuckets.domain.SeverityLevel;
import com.example.incidentbuckets.domain.UserMessage;
import com.example.incidentbuckets.repository.IncidentRepository;
import com.example.incidentbuckets.repository.RawMessageRepository;
import com.example.incidentbuckets.repository.UserMessageRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final UserMessageRepository messageRepository;
    private final RawMessageRepository rawMessageRepository;

    public IncidentService(IncidentRepository incidentRepository, UserMessageRepository messageRepository,
            RawMessageRepository rawMessageRepository) {
        this.incidentRepository = incidentRepository;
        this.messageRepository = messageRepository;
        this.rawMessageRepository = rawMessageRepository;
    }

    /**
     * Ручное создание инцидента оператором: выбранные сырые сообщения переезжают
     * из raw_messages в user_message и привязываются к новому инциденту.
     */
    @Transactional
    public Incident createIncident(String title, SeverityLevel level, List<UUID> rawMessageIds) {
        if (rawMessageIds == null || rawMessageIds.isEmpty()) {
            throw new IllegalArgumentException("Не выбрано ни одного сообщения");
        }
        List<RawMessage> raws = rawMessageRepository.findAllById(rawMessageIds);
        if (raws.isEmpty()) {
            throw new IllegalArgumentException("Выбранные сообщения не найдены");
        }

        Instant now = Instant.now();
        Incident incident = new Incident(title, level, now);
        incidentRepository.save(incident);

        List<UserMessage> messages = new ArrayList<>();
        for (RawMessage raw : raws) {
            incident.registerMessage(raw.getTimestamp());
            messages.add(new UserMessage(raw.getUserId(), raw.getText(), raw.getTimestamp(), incident));
        }
        messageRepository.saveAll(messages);
        rawMessageRepository.deleteAll(raws);
        incidentRepository.save(incident);

        return incident;
    }
}
