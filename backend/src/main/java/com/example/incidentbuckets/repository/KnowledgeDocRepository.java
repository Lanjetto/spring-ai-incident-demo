package com.example.incidentbuckets.repository;

import com.example.incidentbuckets.domain.KnowledgeDoc;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeDocRepository extends JpaRepository<KnowledgeDoc, Long> {

    List<KnowledgeDoc> findAllByOrderByIdAsc();
}
