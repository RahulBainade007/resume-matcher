package com.rahul.resumematcher.evaluation;

public record EvaluationExample(
        String pairId,
        String jobId,
        String resumeId,
        String jobDescription,
        String resumeText,
        int humanRelevanceScore) {

    public EvaluationExample {
        if (pairId == null || pairId.isBlank()) {
            throw new IllegalArgumentException("pairId is required");
        }
        if (jobId == null || jobId.isBlank()) {
            throw new IllegalArgumentException("jobId is required for " + pairId);
        }
        if (resumeId == null || resumeId.isBlank()) {
            throw new IllegalArgumentException("resumeId is required for " + pairId);
        }
        if (jobDescription == null || jobDescription.isBlank()) {
            throw new IllegalArgumentException("jobDescription is required for " + pairId);
        }
        if (resumeText == null || resumeText.isBlank()) {
            throw new IllegalArgumentException("resumeText is required for " + pairId);
        }
        if (humanRelevanceScore < 0 || humanRelevanceScore > 5) {
            throw new IllegalArgumentException("Human relevance score must be between 0 and 5 for " + pairId);
        }
    }
}