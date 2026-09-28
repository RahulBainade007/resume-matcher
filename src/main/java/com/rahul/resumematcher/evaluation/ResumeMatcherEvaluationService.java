package com.rahul.resumematcher.evaluation;

import com.rahul.resumematcher.service.EvidenceScoreResult;
import com.rahul.resumematcher.service.EvidenceScoringService;
import com.rahul.resumematcher.service.MatchingService;
import com.rahul.resumematcher.service.ParsedResume;
import com.rahul.resumematcher.service.ResumeSectionParser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ResumeMatcherEvaluationService {

    private final ResumeSectionParser sectionParser;
    private final EvidenceScoringService evidenceScoringService;
    private final MatchingService matchingService;
    private final EvaluationDatasetLoader datasetLoader;

    public ResumeMatcherEvaluationService(
            ResumeSectionParser sectionParser,
            EvidenceScoringService evidenceScoringService,
            MatchingService matchingService,
            EvaluationDatasetLoader datasetLoader) {
        this.sectionParser = sectionParser;
        this.evidenceScoringService = evidenceScoringService;
        this.matchingService = matchingService;
        this.datasetLoader = datasetLoader;
    }

    public Optional<List<EvaluationResult>> evaluateIfPresent(Path datasetPath) throws IOException {
        if (!Files.exists(datasetPath)) {
            return Optional.empty();
        }
        return Optional.of(evaluate(datasetLoader.load(datasetPath)));
    }

    public List<EvaluationResult> evaluate(List<EvaluationExample> examples) {
        return evaluate(examples, ignored -> Optional.empty());
    }

    public List<EvaluationResult> evaluate(
            List<EvaluationExample> examples,
            Function<EvaluationExample, Optional<Double>> semanticScoreProvider) {
        List<EvaluationResult> results = new ArrayList<>();
        for (EvaluationExample example : examples) {
            ParsedResume parsedResume = sectionParser.parse(example.resumeText());
            Double semanticScore = semanticScoreProvider.apply(example).orElse(null);
            EvidenceScoreResult diagnostic = evidenceScoringService.score(
                    parsedResume, example.jobDescription(), semanticScore);
            double keywordScore = matchingService.match(example.resumeText(), example.jobDescription()).getScore();
            Double baselineScore = semanticScore == null
                    ? null
                    : roundToOneDecimal(keywordScore * 0.50 + semanticScore * 0.50);
            results.add(new EvaluationResult(
                    example.pairId(),
                    example.jobId(),
                    example.resumeId(),
                    diagnostic.getTechnicalSkillScore(),
                    diagnostic.getResponsibilityScore(),
                    diagnostic.getExperienceEvidenceScore(),
                    diagnostic.getProjectEvidenceScore(),
                    diagnostic.getEducationEvidenceScore(),
                    diagnostic.getCertificationEvidenceScore(),
                    keywordScore,
                    semanticScore,
                    baselineScore,
                    example.humanRelevanceScore()));
        }
        return results;
    }

    public EvaluationReport report(List<EvaluationResult> results) {
        List<EvaluationResult> withBaseline = results.stream()
                .filter(result -> result.baselineScore() != null)
                .toList();
        List<Double> predictions = withBaseline.stream().map(result -> result.baselineScore()).toList();
        List<Integer> labels = withBaseline.stream().map(result -> result.humanRelevanceScore()).toList();
        return new EvaluationReport(
                results.size(),
                results.stream().map(result -> result.jobId()).collect(Collectors.toSet()).size(),
            results.stream().map(result -> result.resumeId()).collect(Collectors.toSet()).size(),
            results.stream().map(result -> result.pairId()).collect(Collectors.toSet()).size(),
                (int) results.stream().filter(result -> result.semanticScore() != null).count(),
                EvaluationMetrics.meanAbsoluteError(predictions, labels),
                EvaluationMetrics.spearman(predictions, labels),
                EvaluationMetrics.pearson(predictions, labels.stream().map(score -> score.doubleValue()).toList()),
                EvaluationMetrics.ndcgAtK(withBaseline, 1, result -> result.baselineScore()),
                EvaluationMetrics.ndcgAtK(withBaseline, 3, result -> result.baselineScore()),
                EvaluationMetrics.ndcgAtK(withBaseline, 5, result -> result.baselineScore()));
    }

    public List<EvaluationResult> largestDisagreements(List<EvaluationResult> results, int limit) {
        return results.stream()
                .filter(result -> result.absoluteError() != null)
                .sorted((left, right) -> Double.compare(right.absoluteError(), left.absoluteError()))
                .limit(Math.max(0, limit))
                .toList();
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}