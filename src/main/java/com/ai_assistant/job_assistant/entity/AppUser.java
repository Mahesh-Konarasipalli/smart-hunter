package com.ai_assistant.job_assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private boolean isVerified = false;
    
    private String otpCode;
    
    private LocalDateTime otpExpiryTime;

    // Add these inside your AppUser class:
    @Column(nullable = false)
    private String name = "Agent"; // Default name
    
    @Column(length = 1000)
    private String savedSkills;
    
    private String savedExperience;
}