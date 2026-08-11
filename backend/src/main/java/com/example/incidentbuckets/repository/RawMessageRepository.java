package com.example.incidentbuckets.repository;

import com.example.incidentbuckets.domain.RawMessage;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RawMessageRepository extends JpaRepository<RawMessage, UUID> {

    List<RawMessage> findAllByOrderByTimestampAsc();
}
