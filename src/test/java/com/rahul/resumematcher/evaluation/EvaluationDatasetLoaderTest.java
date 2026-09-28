package com.rahul.resumematcher.evaluation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvaluationDatasetLoaderTest {

    private final EvaluationDatasetLoader loader = new EvaluationDatasetLoader(new ObjectMapper());

    @Test
    void loadsJsonDataset() throws Exception {
        Path file = Files.createTempFile("evaluation", ".json");
        Files.writeString(file, "[{\"pairId\":\"pair-001\",\"jobId\":\"job-001\",\"resumeId\":\"resume-001\",\"jobDescription\":\"Java\",\"resumeText\":\"Java\",\"humanRelevanceScore\":5}]");

        assertEquals(1, loader.load(file).size());
        assertEquals(5, loader.load(file).get(0).humanRelevanceScore());
        assertEquals("job-001", loader.load(file).get(0).jobId());
    }

    @Test
    void rejectsInvalidScoresAndMissingFields() throws Exception {
        Path invalidScore = Files.createTempFile("evaluation-invalid", ".json");
        Files.writeString(invalidScore, "[{\"pairId\":\"pair-x\",\"jobId\":\"job-x\",\"resumeId\":\"resume-x\",\"jobDescription\":\"Java\",\"resumeText\":\"Java\",\"humanRelevanceScore\":6}]");
        Path missingId = Files.createTempFile("evaluation-missing", ".json");
        Files.writeString(missingId, "[{\"pairId\":\"pair-x\",\"jobId\":\"job-x\",\"jobDescription\":\"Java\",\"resumeText\":\"Java\",\"humanRelevanceScore\":2}]");

        var invalidScoreException = assertThrows(IllegalArgumentException.class, () -> loader.load(invalidScore));
        var missingIdException = assertThrows(IllegalArgumentException.class, () -> loader.load(missingId));
        assertEquals(true, invalidScoreException.getMessage().contains("between 0 and 5"));
        assertEquals(true, missingIdException.getMessage().contains("resumeId"));
    }

    @Test
    void validatesHumanScaleAtModelBoundary() {
        var lowerBoundException = assertThrows(IllegalArgumentException.class, () -> new EvaluationExample("pair-x", "job-x", "resume-x", "Java", "Java", -1));
        var upperBoundException = assertThrows(IllegalArgumentException.class, () -> new EvaluationExample("pair-x", "job-x", "resume-x", "Java", "Java", 7));
        assertEquals(true, lowerBoundException.getMessage().contains("between 0 and 5"));
        assertEquals(true, upperBoundException.getMessage().contains("between 0 and 5"));
    }

    @Test
    void rejectsMissingPairJobAndResumeIds() throws Exception {
        assertMissingField("pairId", "job-1", "resume-1");
        assertMissingField("jobId", "job-1", "resume-1");
        assertMissingField("resumeId", "job-1", "resume-1");
    }

    @Test
    void rejectsDuplicatePairIds() throws Exception {
        Path file = Files.createTempFile("evaluation-duplicate", ".json");
        Files.writeString(file, "["
                + "{\"pairId\":\"pair-1\",\"jobId\":\"job-1\",\"resumeId\":\"resume-1\",\"jobDescription\":\"Java\",\"resumeText\":\"Java\",\"humanRelevanceScore\":5},"
                + "{\"pairId\":\"pair-1\",\"jobId\":\"job-1\",\"resumeId\":\"resume-2\",\"jobDescription\":\"Java\",\"resumeText\":\"Java\",\"humanRelevanceScore\":3}]");

        var exception = assertThrows(IllegalArgumentException.class, () -> loader.load(file));
        assertTrue(exception.getMessage().contains("Duplicate pairId"));
    }

    private void assertMissingField(String field, String jobId, String resumeId) throws Exception {
        String pair = field.equals("pairId") ? "" : "\"pairId\":\"pair-1\",";
        String job = field.equals("jobId") ? "" : "\"jobId\":\"" + jobId + "\",";
        String resume = field.equals("resumeId") ? "" : "\"resumeId\":\"" + resumeId + "\",";
        Path file = Files.createTempFile("evaluation-missing-" + field, ".json");
        Files.writeString(file, "[{" + pair + job + resume
                + "\"jobDescription\":\"Java\",\"resumeText\":\"Java\",\"humanRelevanceScore\":2}]");
        var exception = assertThrows(IllegalArgumentException.class, () -> loader.load(file));
        assertTrue(exception.getMessage().contains(field));
    }
}