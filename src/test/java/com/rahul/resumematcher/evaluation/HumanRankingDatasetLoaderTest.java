package com.rahul.resumematcher.evaluation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HumanRankingDatasetLoaderTest {

    private final HumanRankingDatasetLoader loader = new HumanRankingDatasetLoader(new ObjectMapper());

    @Test
    void loadsOfficialNormalizedManifest() throws Exception {
        HumanRankingDataset dataset = loader.load(Path.of("evaluation", "human-ranking", "human-ranking-data.json"));

        assertEquals(5, dataset.vacancies().size());
        assertEquals(30, dataset.annotatedResumes().size());
        assertEquals("CV-001", dataset.annotatedResumes().get(0).resumeId());
        assertEquals(List.of(2, 1, 4, 3, 5), dataset.annotatedResumes().get(0).humanRankAnnotator1());
        assertEquals(List.of(4, 3, 1, 5, 2), dataset.annotatedResumes().get(0).humanRankAnnotator2());
    }

    @Test
    void rejectsWrongVacancyOrResumeCounts() throws Exception {
        HumanRankingDataset dataset = loader.load(Path.of("evaluation", "human-ranking", "human-ranking-data.json"));
        HumanRankingDataset invalid = new HumanRankingDataset(
                dataset.datasetName(), dataset.sourceRepository(), dataset.sourceLicense(), dataset.annotationSource(),
                dataset.vacancies().subList(0, 4), dataset.annotatedResumes());

        Path path = java.nio.file.Files.createTempFile("human-ranking-invalid", ".json");
        new ObjectMapper().writeValue(path.toFile(), invalid);
        var exception = assertThrows(IllegalArgumentException.class, () -> loader.load(path));
        assertEquals(true, exception.getMessage().contains("exactly 5 vacancies"));
    }
}
