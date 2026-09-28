package com.rahul.resumematcher.evaluation;

public record AiEvidenceEvaluation(
        String requirement,
        String category,
        AiEvidenceVerdict verdict,
        AiEvidenceStrength evidenceStrength,
        double confidence,
        String evidenceText,
        String explanation) {
}