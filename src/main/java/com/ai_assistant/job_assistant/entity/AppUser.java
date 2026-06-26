package com.ai_assistant.job_assistant.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

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

   @Column(name = "is_verified")
    private boolean verified = false;
    
    private String otpCode;
    
    private LocalDateTime otpExpiryTime;

    // Add these inside your AppUser class:
    @Column(nullable = false)
    private String name = "Agent"; // Default name
    
    @Column(length = 1000)
    private String savedSkills;
    
    private String savedExperience;
}