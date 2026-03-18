package com.ai_assistant.job_assistant.repository;

import com.ai_assistant.job_assistant.entity.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    // Find all jobs saved by a specific user
    List<SavedJob> findByUserEmail(String userEmail);
    
    // Check if the user already saved this exact link
    boolean existsByUserEmailAndJobUrl(String userEmail, String jobUrl); 
}