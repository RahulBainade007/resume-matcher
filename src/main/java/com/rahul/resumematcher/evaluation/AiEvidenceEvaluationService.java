package com.rahul.resumematcher.evaluation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class AiEvidenceEvaluationService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public AiEvidenceEvaluationService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this(chatClientBuilder.build(), objectMapper);
    }

    AiEvidenceEvaluationService(ChatClient chatClient, ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
    }

    public List<AiEvidenceEvaluation> evaluate(
            AiJobAnalysis analysis,
            List<AiEvidenceCandidate> candidates) {
        if (analysis == null) {
            throw new IllegalArgumentException("job analysis is required");
        }
        if (candidates == null) {
            throw new IllegalArgumentException("evidence candidates are required");
        }

        return candidates.stream()
                .map(candidate -> evaluateCandidate(analysis, candidate))
                .toList();
    }

    private AiEvidenceEvaluation evaluateCandidate(
            AiJobAnalysis analysis,
            AiEvidenceCandidate candidate) {
        if (candidate == null) {
            throw new IllegalArgumentException("evidence candidate is required");
        }
        String category = categoryFor(analysis, candidate.requirement());
        String response = Objects.requireNonNull(
                chatClient.prompt()
                        .user(Objects.requireNonNull(buildPrompt(candidate, category)))
                        .call()
                        .content(),
                "AI response must not be null");
        AiEvidenceEvaluation evaluation = parse(response, objectMapper);
        return validate(evaluation, candidate, category);
    }

    static AiEvidenceEvaluation parse(String response, ObjectMapper objectMapper) {
        if (response == null || response.isBlank()) {
            throw new IllegalArgumentException("AI response must contain JSON");
        }
        try {
            return objectMapper.readValue(response, AiEvidenceEvaluation.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("AI response must be valid evidence JSON", exception);
        }
    }

    private AiEvidenceEvaluation validate(
            AiEvidenceEvaluation evaluation,
            AiEvidenceCandidate candidate,
            String expectedCategory) {
        if (evaluation == null
                || !candidate.requirement().equals(evaluation.requirement())
                || !expectedCategory.equals(evaluation.category())) {
            throw new IllegalArgumentException("AI evidence response does not match the supplied requirement");
        }
        if (evaluation.confidence() < 0.0 || evaluation.confidence() > 1.0) {
            throw new IllegalArgumentException("confidence must be between 0 and 1");
        }
        if (evaluation.evidenceStrength() == null || evaluation.verdict() == null
                || evaluation.explanation() == null || evaluation.explanation().isBlank()) {
            throw new IllegalArgumentException("AI evidence response is incomplete");
        }
        String evidenceText = evaluation.evidenceText() == null ? "" : evaluation.evidenceText().trim();
        if ("SKILLS".equalsIgnoreCase(candidate.section())
                && evaluation.evidenceStrength() == AiEvidenceStrength.DIRECT_EXPERIENCE) {
            throw new IllegalArgumentException("Skills-list evidence cannot be direct experience");
        }
        if (!evidenceText.isEmpty() && !candidate.chunkText().contains(evidenceText)) {
            throw new IllegalArgumentException("evidenceText must come from the supplied resume chunk");
        }
        return new AiEvidenceEvaluation(
                evaluation.requirement(),
                evaluation.category(),
                evaluation.verdict(),
                evaluation.evidenceStrength(),
                evaluation.confidence(),
                evidenceText,
                evaluation.explanation().trim());
    }

    private String categoryFor(AiJobAnalysis analysis, String requirement) {
        if (analysis.technicalSkills().contains(requirement)) {
            return "TECHNICAL_SKILL";
        }
        if (analysis.responsibilities().contains(requirement)) {
            return "RESPONSIBILITY";
        }
        throw new IllegalArgumentException("Requirement is not present in the job analysis");
    }

    private String buildPrompt(AiEvidenceCandidate candidate, String category) {
        return """
                Evaluate the supplied resume evidence against the supplied job requirement.
                Return only one valid JSON object with exactly these fields:
                requirement, category, verdict, evidenceStrength, confidence, evidenceText, explanation.
                Allowed verdicts: MATCHED, PARTIAL, NOT_MATCHED.
                Allowed evidenceStrength values: DIRECT_EXPERIENCE, PROJECT_EVIDENCE, SKILL_LISTING,
                EDUCATION_EVIDENCE, CERTIFICATION_EVIDENCE, NO_EVIDENCE.
                Use only the supplied resume evidence. Do not invent experience, skills, projects, education,
                certifications, or details. A SKILLS section is not direct work experience.
                If evidence is insufficient, use PARTIAL or NOT_MATCHED. evidenceText must be an exact excerpt
                from the supplied chunk, or an empty string when there is no evidence.

                Requirement: %s
                Category: %s
                Resume section: %s
                Resume evidence: %s
                """.formatted(candidate.requirement(), category,
                candidate.section() == null ? "" : candidate.section(), candidate.chunkText());
    }
}