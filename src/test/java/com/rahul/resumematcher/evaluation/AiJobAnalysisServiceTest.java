package com.rahul.resumematcher.evaluation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiJobAnalysisServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void parsesAllStructuredRequirementFields() {
        AiJobAnalysis analysis = AiJobAnalysisService.parse("""
                {
                  "technicalSkills": ["Java", "Spring Boot"],
                  "responsibilities": ["Build APIs"],
                  "experienceRequirements": ["3 years of experience"],
                  "educationRequirements": ["Bachelor's degree"],
                  "certificationRequirements": []
                }
                """, objectMapper);

        assertEquals(List.of("Java", "Spring Boot"), analysis.technicalSkills());
        assertEquals(List.of("Build APIs"), analysis.responsibilities());
        assertEquals(List.of("3 years of experience"), analysis.experienceRequirements());
        assertEquals(List.of("Bachelor's degree"), analysis.educationRequirements());
        assertEquals(List.of(), analysis.certificationRequirements());
    }

    @Test
    void trimsStructuredValues() {
        AiJobAnalysis analysis = AiJobAnalysisService.parse("""
                {"technicalSkills":[" Java "],"responsibilities":[],"experienceRequirements":[],"educationRequirements":[],"certificationRequirements":[]}
                """, objectMapper);

        assertEquals(List.of("Java"), analysis.technicalSkills());
    }

    @Test
    void rejectsMalformedOrIncompleteResponses() {
        assertThrows(IllegalArgumentException.class, () -> AiJobAnalysisService.parse("not json", objectMapper));
        assertThrows(IllegalArgumentException.class, () -> AiJobAnalysisService.parse(
            "{\"technicalSkills\":null,\"responsibilities\":[],\"experienceRequirements\":[],\"educationRequirements\":[],\"certificationRequirements\":[]}", objectMapper));
        assertThrows(IllegalArgumentException.class, () -> AiJobAnalysisService.parse(
            "{\"technicalSkills\":[\" \"],\"responsibilities\":[],\"experienceRequirements\":[],\"educationRequirements\":[],\"certificationRequirements\":[]}", objectMapper));
    }
}