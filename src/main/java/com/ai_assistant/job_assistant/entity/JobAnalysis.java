package com.ai_assistant.job_assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "job_evaluations")
@Data // Lombok automatically generates getters and setters
@NoArgsConstructor
public class JobAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String jobUrl;

    private int matchPercentage;

    @Column(columnDefinition = "TEXT")
    private String briefSummary;

    @ElementCollection // Tells Hibernate to create a separate table for this list of strings
    private List<String> missingSkills;

    @Column(columnDefinition = "TEXT")
    private String recommendation;
}