package com.example.incidentbuckets.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "knowledge_doc")
public class KnowledgeDoc {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "knowledge_doc_seq")
    @SequenceGenerator(name = "knowledge_doc_seq", sequenceName = "knowledge_doc_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String symptom;

    @Column(nullable = false)
    private String resolution;

    protected KnowledgeDoc() {
    }

    public KnowledgeDoc(String title, String symptom, String resolution) {
        this.title = title;
        this.symptom = symptom;
        this.resolution = resolution;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSymptom() {
        return symptom;
    }

    public String getResolution() {
        return resolution;
    }
}
