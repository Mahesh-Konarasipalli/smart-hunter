package com.ai_assistant.job_assistant.repository;

import com.ai_assistant.job_assistant.entity.JobAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobAnalysisRepository extends JpaRepository<JobAnalysis, Long> {
}