package com.rahul.resumematcher.evaluation;

import java.util.List;

public record HumanRankingDataset(
        String datasetName,
        String sourceRepository,
        String sourceLicense,
        String annotationSource,
        List<HumanRankingVacancy> vacancies,
        List<HumanRankingResume> annotatedResumes) {
}
