package com.rahul.resumematcher.evaluation;

import com.rahul.resumematcher.service.EvidenceMatchingService;
import com.rahul.resumematcher.service.EvidenceScoringService;
import com.rahul.resumematcher.service.MatchingService;
import com.rahul.resumematcher.service.ResponsibilityMatcherService;
import com.rahul.resumematcher.service.ResumeSectionParser;
import com.rahul.resumematcher.service.SkillMatcherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvaluationRunnerTest {

    private final ResumeMatcherEvaluationService runner = new ResumeMatcherEvaluationService(
            new ResumeSectionParser(),
            new EvidenceScoringService(new EvidenceMatchingService(
                    new SkillMatcherService(), new ResponsibilityMatcherService())),
            new MatchingService(), new EvaluationDatasetLoader(new ObjectMapper()));

    @Test
    void evaluatesSyntheticExamplesAndLeavesSemanticUnavailable() {
        List<EvaluationResult> results = runner.evaluate(List.of(
                new EvaluationExample("pair-001", "job-001", "resume-001", "Java, Spring Boot, Docker", "SKILLS\nJava, Spring Boot, Docker", 5),
                new EvaluationExample("pair-002", "job-001", "resume-002", "Java, Spring Boot, Docker", "SKILLS\nJava", 3)));

        assertEquals(2, results.size());
        assertNull(results.get(0).semanticScore());
        assertNull(results.get(0).baselineScore());
        assertEquals(100.0, results.get(0).technicalSkillScore());
        assertEquals(33.3, results.get(1).technicalSkillScore());
        assertEquals(33.3, results.get(1).keywordScore());
        assertNull(results.get(1).absoluteError());
    }

    @Test
    void preservesRealSemanticScoreAndBaselineSeparately() {
        EvaluationExample example = new EvaluationExample(
                "pair-001", "job-001", "resume-001", "Java, Spring Boot", "SKILLS\nJava, Spring Boot", 5);

        EvaluationResult result = runner.evaluate(List.of(example), ignored -> Optional.of(60.0)).get(0);

        assertEquals(60.0, result.semanticScore());
        assertEquals(80.0, result.baselineScore());
        assertEquals(20.0, result.absoluteError());
    }

    @Test
    void reportsEmptyAndSingleExampleDatasetsWithoutFabricatingCorrelations() {
        assertEquals(0, runner.report(List.of()).numberOfExamples());
        EvaluationResult result = runner.evaluate(List.of(
                new EvaluationExample("pair-one", "job-one", "resume-one", "Java", "SKILLS\nJava", 5))).get(0);
        assertNull(runner.report(List.of(result)).spearmanCorrelation());
        assertNull(runner.report(List.of(result)).pearsonCorrelation());
                assertNull(runner.report(List.of(result)).ndcgAt1());
    }

        @Test
        void missingRealDatasetIsHandledWithoutEvaluation() throws Exception {
                assertTrue(runner.evaluateIfPresent(java.nio.file.Path.of("evaluation", "real-evaluation-data.json")).isEmpty());
        }

        @Test
        void preservesExplicitResumeAndJobCountsAndFindsLargestDisagreements() {
                List<EvaluationExample> examples = List.of(
                                new EvaluationExample("pair-1", "job-1", "resume-1", "Java", "SKILLS\nJava", 5),
                                new EvaluationExample("pair-2", "job-1", "resume-2", "Java", "SKILLS\nJava", 1),
                                new EvaluationExample("pair-3", "job-2", "resume-1", "Java", "SKILLS\nJava", 3));
                List<EvaluationResult> results = runner.evaluate(examples, ignored -> Optional.of(50.0));

                assertEquals(2, runner.report(results).numberOfJobGroups());
                assertEquals(2, runner.report(results).numberOfUniqueResumes());
                assertEquals(3, runner.report(results).numberOfUniquePairs());
                assertEquals("pair-2", runner.largestDisagreements(results, 1).get(0).pairId());
        }
}