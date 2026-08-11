package com.example.incidentbuckets.repository;

import com.example.incidentbuckets.domain.Incident;
import com.example.incidentbuckets.domain.SeverityLevel;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface IncidentRepository extends JpaRepository<Incident, UUID> {

    @Query("select i from Incident i where i.level = :level and i.closedAt is null order by i.lastMessageAt desc")
    List<Incident> findOpenByLevelOrderByLastMessageAtDesc(SeverityLevel level);

    @Query("select count(i) from Incident i where i.level = :level and i.closedAt is null")
    long countOpenByLevel(SeverityLevel level);

    @Query("select coalesce(sum(i.messageCount), 0) from Incident i where i.level = :level and i.closedAt is null")
    long sumOpenMessageCountByLevel(SeverityLevel level);
}
