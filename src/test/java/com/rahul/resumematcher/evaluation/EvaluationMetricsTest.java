package com.rahul.resumematcher.evaluation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EvaluationMetricsTest {

    @Test
    void calculatesMaeOnNormalizedHundredPointScale() {
        assertEquals(5.0, EvaluationMetrics.meanAbsoluteError(
                List.of(100.0, 50.0), List.of(5, 2)));
    }

    @Test
    void calculatesSpearmanAndPearson() {
        assertEquals(1.0, EvaluationMetrics.spearman(List.of(10.0, 20.0, 30.0), List.of(1, 2, 3)));
        assertEquals(1.0, EvaluationMetrics.pearson(List.of(10.0, 20.0, 30.0), List.of(1.0, 2.0, 3.0)));
    }

    @Test
    void handlesTiesAndInsufficientSamples() {
        assertEquals(1.0, EvaluationMetrics.spearman(List.of(10.0, 10.0, 30.0), List.of(1, 1, 3)));
        assertNull(EvaluationMetrics.pearson(List.of(10.0), List.of(1.0)));
        assertNull(EvaluationMetrics.spearman(List.of(10.0, 10.0), List.of(1, 2)));
    }

    @Test
    void calculatesGradedNdcgAtOneThreeAndFive() {
        List<EvaluationResult> results = List.of(
                result("pair-a", "job-001", "resume-a", 5, 90),
                result("pair-b", "job-001", "resume-b", 4, 80),
                result("pair-c", "job-001", "resume-c", 2, 60),
                result("pair-d", "job-001", "resume-d", 1, 40));

        assertEquals(1.0, EvaluationMetrics.ndcgAtK(results, 1, result -> result.baselineScore()));
        assertEquals(1.0, EvaluationMetrics.ndcgAtK(results, 3, result -> result.baselineScore()));
        assertEquals(1.0, EvaluationMetrics.ndcgAtK(results, 5, result -> result.baselineScore()));
    }

    @Test
    void supportsMultipleGroupsAndEmptyInputs() {
        List<EvaluationResult> results = List.of(
                result("pair-a", "job-001", "resume-a", 5, 90),
                result("pair-b", "job-001", "resume-b", 1, 10),
                result("pair-c", "job-002", "resume-c", 3, 50));

        assertEquals(2, results.stream().map(result -> result.jobId()).distinct().count());
        assertNull(EvaluationMetrics.ndcgAtK(List.of(), 3, result -> result.baselineScore()));
    }

    @Test
    void ndcgUsesJobIdWhenDescriptionsWouldOtherwiseCollide() {
        List<EvaluationResult> results = List.of(
                result("pair-a", "job-001", "resume-a", 5, 90),
                result("pair-b", "job-001", "resume-b", 1, 10),
                result("pair-c", "job-002", "resume-c", 1, 90),
                result("pair-d", "job-002", "resume-d", 5, 10));

        assertEquals((1.0 + (1.0 / 31.0)) / 2.0,
            EvaluationMetrics.ndcgAtK(results, 1, item -> item.baselineScore()), 0.0001);
    }

    private EvaluationResult result(String pairId, String jobId, String resumeId, int humanScore, double baseline) {
        return new EvaluationResult(pairId, jobId, resumeId, 0, 0, 0, 0, 0, 0, 0,
                null, baseline, humanScore);
    }
}