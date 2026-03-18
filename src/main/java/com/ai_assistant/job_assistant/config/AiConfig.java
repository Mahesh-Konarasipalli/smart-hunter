package com.ai_assistant.job_assistant.config;

import com.ai_assistant.job_assistant.service.JobAnalyst;
import com.ai_assistant.job_assistant.service.ResumeAnalyzer;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel; // Using OpenAI compatible client
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

@Configuration
public class AiConfig {

    // This pulls the key we will set in Railway variables
    @Value("${GROQ_API_KEY:offline}")
    private String groqApiKey;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        // If we are in the cloud (Railway), use Groq's lightning fast Llama 3
        if (!"offline".equals(groqApiKey)) {
            System.out.println("🚀 Cloud AI Active: Using Groq Llama 3");
            return OpenAiChatModel.builder()
                    .apiKey(groqApiKey)
                    .baseUrl("https://api.groq.com/openai/v1") // Point to Groq
                    .modelName("llama3-8b-8192") 
                    .timeout(Duration.ofMinutes(1))
                    .temperature(0.0)
                    .logRequests(true)
                    .logResponses(true)
                    .build();
        }

        // FALLBACK: If no key is found, try to use your local Ollama (for your laptop)
        System.out.println("🏠 Local AI Active: Using Ollama");
        return dev.langchain4j.model.ollama.OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("llama3")
                .format("json")
                .build();
    }

    @Bean
    public JobAnalyst jobAnalyst(ChatLanguageModel chatModel) {
        return AiServices.builder(JobAnalyst.class)
                .chatLanguageModel(chatModel)
                .build();
    }

    @Bean
    public ResumeAnalyzer resumeAnalyzer(ChatLanguageModel chatModel) {
        return AiServices.builder(ResumeAnalyzer.class)
                .chatLanguageModel(chatModel)
                .build();
    }
}