package com.rahul.resumematcher.evaluation;

public record EvaluationReport(
        int numberOfExamples,
        int numberOfJobGroups,
        int numberOfUniqueResumes,
        int numberOfUniquePairs,
        int availableSemanticScores,
        Double meanAbsoluteError,
        Double spearmanCorrelation,
        Double pearsonCorrelation,
        Double ndcgAt1,
        Double ndcgAt3,
        Double ndcgAt5) {

        public Double normalizedMeanAbsoluteError() {
                return meanAbsoluteError;
        }
}