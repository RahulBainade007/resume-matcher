package com.rahul.resumematcher.evaluation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

public final class EvaluationMetrics {

    private EvaluationMetrics() {
    }

    public static Double meanAbsoluteError(List<Double> predictions, List<Integer> humanScores) {
        if (predictions.isEmpty() || predictions.size() != humanScores.size()) {
            return null;
        }
        double total = 0.0;
        for (int index = 0; index < predictions.size(); index++) {
            total += Math.abs(predictions.get(index) - humanScores.get(index) * 20.0);
        }
        return total / predictions.size();
    }

    public static Double spearman(List<Double> predictions, List<Integer> humanScores) {
        if (predictions.size() < 2 || predictions.size() != humanScores.size()) {
            return null;
        }
        return pearson(rank(predictions), rank(humanScores.stream().map(score -> score.doubleValue()).toList()));
    }

    public static Double pearson(List<Double> first, List<Double> second) {
        if (first.size() < 2 || first.size() != second.size()) {
            return null;
        }
        double firstMean = first.stream().mapToDouble(value -> value).average().orElse(0.0);
        double secondMean = second.stream().mapToDouble(value -> value).average().orElse(0.0);
        double numerator = 0.0;
        double firstVariance = 0.0;
        double secondVariance = 0.0;
        for (int index = 0; index < first.size(); index++) {
            double firstDelta = first.get(index) - firstMean;
            double secondDelta = second.get(index) - secondMean;
            numerator += firstDelta * secondDelta;
            firstVariance += firstDelta * firstDelta;
            secondVariance += secondDelta * secondDelta;
        }
        if (firstVariance == 0.0 || secondVariance == 0.0) {
            return null;
        }
        return numerator / Math.sqrt(firstVariance * secondVariance);
    }

    public static Double ndcgAtK(
            List<EvaluationResult> results,
            int k,
            ToDoubleFunction<EvaluationResult> predictedScore) {
        if (results.isEmpty() || k <= 0) {
            return null;
        }
        Map<String, List<EvaluationResult>> groups = new HashMap<>();
        for (EvaluationResult result : results) {
            groups.computeIfAbsent(result.jobId(), ignored -> new ArrayList<>()).add(result);
        }
        return groups.values().stream()
                .mapToDouble(group -> ndcgForGroup(group, k, predictedScore))
                .average()
                .orElse(Double.NaN);
    }

    private static double ndcgForGroup(
            List<EvaluationResult> group,
            int k,
            ToDoubleFunction<EvaluationResult> predictedScore) {
        List<EvaluationResult> predicted = group.stream()
                .sorted(Comparator.comparingDouble(predictedScore).reversed())
                .limit(k)
                .toList();
        List<EvaluationResult> ideal = group.stream()
                .sorted(Comparator.<EvaluationResult>comparingInt(result -> result.humanRelevanceScore()).reversed())
                .limit(k)
                .toList();
        double dcg = discountedGain(predicted);
        double idealDcg = discountedGain(ideal);
        return idealDcg == 0.0 ? 0.0 : dcg / idealDcg;
    }

    private static double discountedGain(List<EvaluationResult> results) {
        double gain = 0.0;
        for (int index = 0; index < results.size(); index++) {
            gain += (Math.pow(2.0, results.get(index).humanRelevanceScore()) - 1.0)
                    / (Math.log(index + 2.0) / Math.log(2.0));
        }
        return gain;
    }

    private static List<Double> rank(List<Double> values) {
        List<Integer> indexes = new ArrayList<>();
        for (int index = 0; index < values.size(); index++) {
            indexes.add(index);
        }
        indexes.sort(Comparator.comparingDouble((Integer index) -> values.get(index)));
        double[] ranks = new double[values.size()];
        int start = 0;
        while (start < indexes.size()) {
            int end = start + 1;
            while (end < indexes.size() && Double.compare(values.get(indexes.get(start)), values.get(indexes.get(end))) == 0) {
                end++;
            }
            double averageRank = (start + 1 + end) / 2.0;
            for (int position = start; position < end; position++) {
                ranks[indexes.get(position)] = averageRank;
            }
            start = end;
        }
        return java.util.Arrays.stream(ranks).boxed().toList();
    }
}