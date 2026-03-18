package com.ai_assistant.job_assistant.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

// 👇 IMPORTANT: No @AiService, @Service, or @Component annotations here!
public interface JobAnalyst {
    
    @SystemMessage("""
        You are an expert technical recruiter. Analyze this web page text against a candidate.
        If the text is an article, a blog, or NOT a job posting, immediately return a matchPercentage of 0.
        """)
   JobAnalysisResult analyzeJob(@UserMessage String jobDescription);
    // Add this inside your JobAnalyst interface!
    @dev.langchain4j.service.SystemMessage("You are an expert executive career coach and resume writer.")
    @dev.langchain4j.service.UserMessage("""
        Write a highly professional, 3-paragraph cover letter for {{name}} applying for the following role: {{jobSummary}}. 
        
        The candidate possesses these specific skills: {{skills}}. 
        
        Make the letter engaging, confident, and perfectly tailored to how their skills solve the needs of the role. 
        Do not include placeholder brackets like [Company Name] if you don't know it, just write naturally. 
        Return ONLY the text of the cover letter, with no extra pleasantries or formatting.
        """)
    String generateCoverLetter(@dev.langchain4j.service.V("name") String name, 
                               @dev.langchain4j.service.V("skills") String skills, 
                               @dev.langchain4j.service.V("jobSummary") String jobSummary);
}