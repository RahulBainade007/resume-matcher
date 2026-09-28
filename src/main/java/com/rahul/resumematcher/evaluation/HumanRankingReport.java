package com.rahul.resumematcher.evaluation;

import java.util.List;

public record HumanRankingReport(
        String reportType,
        String source,
        int vacancies,
        int annotatedResumes,
        int expectedPairs,
        int evaluatedPairs,
        int failedPairs,
        int annotators,
        double keywordWeight,
        double semanticWeight,
        String baselineDescription,
        Double ndcgAt1,
        Double ndcgAt3,
        Double ndcgAt5,
        Double spearman,
        List<HumanRankingResumeResult> perResumeResults,
        List<String> rankingDisagreementCases) {
}