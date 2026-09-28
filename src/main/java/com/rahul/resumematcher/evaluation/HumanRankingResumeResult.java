package com.rahul.resumematcher.evaluation;

import java.util.List;

public record HumanRankingResumeResult(
        String resumeId,
        List<Integer> humanRankingAnnotator1,
        List<Integer> humanRankingAnnotator2,
        List<String> systemRanking,
        Double ndcgAt1,
        Double ndcgAt3,
        Double ndcgAt5,
        Double spearman,
        List<HumanRankingPairResult> pairs) {
}