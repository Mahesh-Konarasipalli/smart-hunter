package com.ai_assistant.job_assistant.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ai_assistant.job_assistant.service.JobAnalyst;
import com.ai_assistant.job_assistant.service.ResumeAnalyzer;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

@Configuration // ⚡ CRUCIAL: Tells Spring this file contains setup instructions
public class AiConfig {

    @Value("${GROQ_API_KEY:offline}")
    private String groqApiKey;

    @Bean // ⚡ CRUCIAL: Tells Spring to build this AI Model
    public ChatLanguageModel chatLanguageModel() {
        if (!"offline".equals(groqApiKey)) {
            System.out.println("🚀 Cloud AI Active: Using Groq Llama 3");
            return OpenAiChatModel.builder()
                    .apiKey(groqApiKey)
                    .baseUrl("https://api.groq.com/openai/v1")
                    .modelName("llama3-8b-8192")
                    .timeout(Duration.ofMinutes(1))
                    .temperature(0.0)
                    .logRequests(true)
                    .logResponses(true)
                    .build();
        }

        System.out.println("🏠 Local AI Active: Using Ollama");
        return dev.langchain4j.model.ollama.OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("llama3")
                .format("json")
                .build();
    }

    @Bean // ⚡ CRUCIAL: Tells Spring how to build the JobAnalyst!
    public JobAnalyst jobAnalyst(ChatLanguageModel chatModel) {
        return AiServices.builder(JobAnalyst.class)
                .chatLanguageModel(chatModel)
                .build();
    }

    @Bean // ⚡ CRUCIAL: Tells Spring how to build the ResumeAnalyzer!
    public ResumeAnalyzer resumeAnalyzer(ChatLanguageModel chatModel) {
        return AiServices.builder(ResumeAnalyzer.class)
                .chatLanguageModel(chatModel)
                .build();
    }
}