package com.rahul.resumematcher.evaluation;

public record HumanRankingVacancy(
        String jobId,
        int sourceRowId,
        String sourceUid,
        String sourceFile) {
}
