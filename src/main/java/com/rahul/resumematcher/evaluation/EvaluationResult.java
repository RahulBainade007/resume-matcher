package com.rahul.resumematcher.evaluation;

public record EvaluationResult(
        String pairId,
        String jobId,
        String resumeId,
        double technicalSkillScore,
        double responsibilityScore,
        double experienceEvidenceScore,
        double projectEvidenceScore,
        double educationEvidenceScore,
        double certificationEvidenceScore,
                double keywordScore,
        Double semanticScore,
        Double baselineScore,
        int humanRelevanceScore) {

        public Double systemScore() {
                return baselineScore;
        }

        public Double absoluteError() {
                return baselineScore == null
                                ? null
                                : Math.abs(baselineScore - humanRelevanceScore * 20.0);
        }
}