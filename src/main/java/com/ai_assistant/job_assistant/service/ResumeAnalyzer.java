package com.ai_assistant.job_assistant.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface ResumeAnalyzer {
    
       @SystemMessage("""
        Extract the candidate's core technical stack and experience level from the resume.
        Return JSON with exactly two keys: 'searchKeywords' and 'experienceLevel'.
        
        RULES FOR EXPERIENCE:
        1. 'searchKeywords': Limit to EXACTLY the 3 most important technical skills (e.g., 'Java, Spring Boot, MySQL'). DO NOT list more than 3.
        2. 'experienceLevel': If the resume only lists projects and education without paid professional employment, you MUST output exactly the word 'fresher'. Do not use the word junior.
        """)
    ResumeSkills extractSkills(@UserMessage String resumeText);
    
}