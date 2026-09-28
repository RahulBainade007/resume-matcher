package com.rahul.resumematcher.evaluation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiEvidenceEvaluationServiceTest {

    private final ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
    private final AiEvidenceEvaluationService service = new AiEvidenceEvaluationService(
            chatClient, new ObjectMapper());

    @Test
    void evaluatesEvidenceUsingStructuredLlmResponse() {
        when(chatClient.prompt().user(anyString()).call().content()).thenReturn("""
                {
                  "requirement": "Java",
                  "category": "TECHNICAL_SKILL",
                  "verdict": "MATCHED",
                  "evidenceStrength": "DIRECT_EXPERIENCE",
                  "confidence": 0.92,
                  "evidenceText": "Developed Java services",
                  "explanation": "The evidence describes direct Java work."
                }
                """);

        AiEvidenceEvaluation evaluation = service.evaluate(
                new AiJobAnalysis(List.of("Java"), List.of(), List.of(), List.of(), List.of()),
                List.of(new AiEvidenceCandidate(
                        "Java", "Developed Java services", "EXPERIENCE", 0.91)))
                .get(0);

        assertEquals("TECHNICAL_SKILL", evaluation.category());
        assertEquals(AiEvidenceVerdict.MATCHED, evaluation.verdict());
        assertEquals(AiEvidenceStrength.DIRECT_EXPERIENCE, evaluation.evidenceStrength());
        assertEquals(0.92, evaluation.confidence());
        assertEquals("Developed Java services", evaluation.evidenceText());
    }

    @Test
    void rejectsDirectExperienceClaimFromSkillsSection() {
        when(chatClient.prompt().user(anyString()).call().content()).thenReturn("""
                {"requirement":"Java","category":"TECHNICAL_SKILL","verdict":"MATCHED","evidenceStrength":"DIRECT_EXPERIENCE","confidence":0.9,"evidenceText":"Java","explanation":"Listed Java."}
                """);

        assertThrows(IllegalArgumentException.class, () -> service.evaluate(
                new AiJobAnalysis(List.of("Java"), List.of(), List.of(), List.of(), List.of()),
                List.of(new AiEvidenceCandidate("Java", "Java", "SKILLS", 0.9))));
    }

    @Test
    void rejectsEvidenceOutsideSuppliedChunkAndInvalidConfidence() {
        when(chatClient.prompt().user(anyString()).call().content()).thenReturn("""
                {"requirement":"Build APIs","category":"RESPONSIBILITY","verdict":"PARTIAL","evidenceStrength":"PROJECT_EVIDENCE","confidence":1.2,"evidenceText":"Invented project","explanation":"Insufficient evidence."}
                """);

        assertThrows(IllegalArgumentException.class, () -> service.evaluate(
                new AiJobAnalysis(List.of(), List.of("Build APIs"), List.of(), List.of(), List.of()),
                List.of(new AiEvidenceCandidate("Build APIs", "Maintained services", "PROJECTS", 0.8))));
    }
}