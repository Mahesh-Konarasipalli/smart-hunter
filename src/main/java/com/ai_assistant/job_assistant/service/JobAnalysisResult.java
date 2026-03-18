package com.ai_assistant.job_assistant.service;

import java.util.List;

// A Java Record automatically maps to a JSON object
public record JobAnalysisResult(
    int matchPercentage,
    String briefSummary,
    List<String> missingSkills,
    String recommendation,
    String jobUrl
) {}