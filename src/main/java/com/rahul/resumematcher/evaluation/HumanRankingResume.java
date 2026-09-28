package com.rahul.resumematcher.evaluation;

import java.util.List;

public record HumanRankingResume(
        String resumeId,
        String sourceFile,
        List<Integer> humanRankAnnotator1,
        List<Integer> humanRankAnnotator2) {
}
