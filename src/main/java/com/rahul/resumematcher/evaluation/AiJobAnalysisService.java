package com.rahul.resumematcher.evaluation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class AiJobAnalysisService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public AiJobAnalysisService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public AiJobAnalysis analyze(String jobDescription) {
        if (jobDescription == null || jobDescription.isBlank()) {
            throw new IllegalArgumentException("jobDescription is required");
        }

        String response = chatClient
                .prompt()
                .user(Objects.requireNonNull(buildPrompt(jobDescription)))
                .call()
                .content();
        return parse(Objects.requireNonNull(response, "AI response must not be null"), objectMapper);
    }

    static AiJobAnalysis parse(String response, ObjectMapper objectMapper) {
        if (response == null || response.isBlank()) {
            throw new IllegalArgumentException("AI response must contain JSON");
        }
        try {
            AiJobAnalysis analysis = objectMapper.readValue(response, AiJobAnalysis.class);
            return validate(analysis);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("AI response must be valid JSON", exception);
        }
    }

    private static AiJobAnalysis validate(AiJobAnalysis analysis) {
        if (analysis == null) {
            throw new IllegalArgumentException("AI response must contain an analysis object");
        }
        return new AiJobAnalysis(
                validateList("technicalSkills", analysis.technicalSkills()),
                validateList("responsibilities", analysis.responsibilities()),
                validateList("experienceRequirements", analysis.experienceRequirements()),
                validateList("educationRequirements", analysis.educationRequirements()),
                validateList("certificationRequirements", analysis.certificationRequirements()));
    }

    private static List<String> validateList(String field, List<String> values) {
        if (values == null) {
            throw new IllegalArgumentException("AI response is missing " + field);
        }
        return values.stream()
                .map(value -> value == null ? null : value.trim())
                .peek(value -> {
                    if (value == null || value.isEmpty()) {
                        throw new IllegalArgumentException(field + " must not contain blank values");
                    }
                })
                .toList();
    }

    private String buildPrompt(String jobDescription) {
        return """
                Analyze the job description below. Return only one valid JSON object with exactly these array fields:
                technicalSkills, responsibilities, experienceRequirements, educationRequirements, certificationRequirements.
                Copy only requirements explicitly present in the job description. Do not infer, expand, or invent requirements.
                Use an empty array when a category is not present. Every array value must be a concise string from the job description.

                Job description:
                %s
                """.formatted(jobDescription);
    }
}