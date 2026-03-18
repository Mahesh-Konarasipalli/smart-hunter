package com.ai_assistant.job_assistant.config;

import com.ai_assistant.job_assistant.service.JobAnalyst;
import com.ai_assistant.job_assistant.service.ResumeAnalyzer;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AiConfig {

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("llama3")
                .timeout(Duration.ofMinutes(15))
                .temperature(0.0)
                .format("json") 
                .build();
    }

    @Bean
    public JobAnalyst jobAnalyst(ChatLanguageModel chatModel) {
        return AiServices.builder(JobAnalyst.class)
                .chatLanguageModel(chatModel)
                .build();
    }

    // Bean 2: Analyzes Resumes
    @Bean
    public ResumeAnalyzer resumeAnalyzer(ChatLanguageModel chatModel) {
        return AiServices.builder(ResumeAnalyzer.class)
                .chatLanguageModel(chatModel)
                .build();
    }
}