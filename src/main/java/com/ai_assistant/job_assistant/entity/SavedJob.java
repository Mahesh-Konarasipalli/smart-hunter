package com.ai_assistant.job_assistant.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "saved_jobs")
@Data
public class SavedJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // This links the job to the person logged in!
    @Column(nullable = false)
    private String userEmail; 
    
    @Column(length = 2000)
    private String briefSummary;
    
    @Column(nullable = false, length = 1000)
    private String jobUrl;
    
    private int matchPercentage;
    
    @Column(length = 2000)
    private String recommendation;

    @Column(length = 1000)
    private String missingSkills;

    @Column(nullable = false)
    private String status = "SAVED";
}