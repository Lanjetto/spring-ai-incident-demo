package com.example.incidentbuckets.repository;

import com.example.incidentbuckets.domain.UserMessage;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMessageRepository extends JpaRepository<UserMessage, UUID> {

    List<UserMessage> findByIncidentIdOrderByReceivedAtAsc(UUID incidentId);
}
