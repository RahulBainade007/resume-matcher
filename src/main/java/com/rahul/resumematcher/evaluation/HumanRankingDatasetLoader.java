package com.rahul.resumematcher.evaluation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class HumanRankingDatasetLoader {

    private final ObjectMapper objectMapper;

    public HumanRankingDatasetLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public HumanRankingDataset load(Path path) throws IOException {
        HumanRankingDataset dataset = objectMapper.readValue(Files.readString(path), HumanRankingDataset.class);
        validate(dataset);
        return dataset;
    }

    private void validate(HumanRankingDataset dataset) {
        if (dataset == null || dataset.vacancies() == null || dataset.annotatedResumes() == null) {
            throw new IllegalArgumentException("Human-ranking dataset must contain vacancies and annotatedResumes");
        }
        if (dataset.vacancies().size() != 5) {
            throw new IllegalArgumentException("Human-ranking dataset must contain exactly 5 vacancies");
        }
        if (dataset.annotatedResumes().size() != 30) {
            throw new IllegalArgumentException("Human-ranking dataset must contain exactly 30 annotated resumes");
        }

        Set<String> jobIds = new HashSet<>();
        for (HumanRankingVacancy vacancy : dataset.vacancies()) {
            if (vacancy.jobId() == null || vacancy.jobId().isBlank() || !jobIds.add(vacancy.jobId())) {
                throw new IllegalArgumentException("Vacancy job IDs must be unique and non-blank");
            }
        }

        Set<String> resumeIds = new HashSet<>();
        for (HumanRankingResume resume : dataset.annotatedResumes()) {
            if (resume.resumeId() == null || resume.resumeId().isBlank() || !resumeIds.add(resume.resumeId())) {
                throw new IllegalArgumentException("Annotated resume IDs must be unique and non-blank");
            }
            validateRanks(resume.humanRankAnnotator1(), resume.resumeId(), "annotator 1");
            validateRanks(resume.humanRankAnnotator2(), resume.resumeId(), "annotator 2");
        }
    }

    private void validateRanks(List<Integer> ranks, String resumeId, String annotator) {
        if (ranks == null || ranks.size() != 5 || ranks.stream().anyMatch(rank -> rank == null || rank < 1 || rank > 5)) {
            throw new IllegalArgumentException("Each " + annotator + " ranking for " + resumeId + " must contain five values from 1 to 5");
        }
    }
}
