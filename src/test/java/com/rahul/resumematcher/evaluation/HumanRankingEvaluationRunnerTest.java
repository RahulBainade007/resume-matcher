package com.rahul.resumematcher.evaluation;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HumanRankingEvaluationRunnerTest {

    @Test
    void generatedDatabaseResumeIdFitsDocumentChunkColumn() {
        assertTrue(HumanRankingEvaluationService.createRunResumeId().length() <= 36);
    }

    @Autowired
    private HumanRankingEvaluationService evaluationService;

    @Test
    void runsOfficialHumanRankingBaselineWhenEnabled() throws Exception {
        Assumptions.assumeTrue(Boolean.getBoolean("humanRanking.run"));
        Path sourceRoot = Path.of(System.getProperty("humanRanking.sourceRoot"));
        evaluationService.evaluate(
                Path.of("evaluation", "human-ranking", "human-ranking-data.json"),
                sourceRoot,
                Path.of("evaluation", "human-ranking", "human-ranking-report.json"),
                Path.of("evaluation", "human-ranking", "human-ranking-report.md"));
    }
}