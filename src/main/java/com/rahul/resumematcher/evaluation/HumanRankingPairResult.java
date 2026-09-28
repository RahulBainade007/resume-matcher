package com.rahul.resumematcher.evaluation;

public record HumanRankingPairResult(
        String jobId,
        Double keywordScore,
        Double semanticScore,
        Double systemScore,
        int humanRankAnnotator1,
        int humanRankAnnotator2,
        String failure) {

    public static HumanRankingPairResult failure(String jobId, int rank1, int rank2, String message) {
        return new HumanRankingPairResult(jobId, null, null, null, rank1, rank2, message);
    }
}