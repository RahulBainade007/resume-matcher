# Human-Ranking Benchmark

This is a separate ranking-based evaluation track. It is not part of the 0-5 `EvaluationExample` benchmark and must not be mixed with `real-evaluation-data.json` or synthetic test data.

## Source

Official repository: https://github.com/NataliaVanetik/vacancy-resume-matching-dataset

Source license: GPL-3.0. The repository contains 5 selected vacancies, 65 anonymized DOCX resumes, and human rankings for CV files 1 through 30. The normalized manifest stores source filenames and vacancy row identifiers so records remain traceable without copying the source resumes into this project.

Research citation:

Natalia Vanetik and Genady Kogan, "Job Vacancy Ranking with Sentence Embeddings, Keywords, and Named Entities", *Information*, 2023, 14(8), 468. DOI: 10.3390/info14080468.

This benchmark is used for research and evaluation purposes.

## Annotation Semantics

The source file `annotations-for-the-first-30-vacancies.txt` contains two independent annotator arrays for each CV 1 through 30. Each array is preserved exactly in `human-ranking-data.json` as `humanRankAnnotator1` and `humanRankAnnotator2`. The source describes the five array positions as vacancies 1 through 5 and the values as their annotation positions/ranks. No annotator is discarded and no combined rank is invented.

The normalized records are traceable through:

- `resumeId` and `sourceFile`, such as `CV-001` and `CV/1.docx`.
- `jobId`, `sourceRowId`, `sourceUid`, and `sourceFile`, mapped to the official `5_vacancies.csv`.
- The two raw annotation arrays copied from the official annotation file.

Two source arrays contain repeated positions rather than a strict permutation (`CV-009` and `CV-028`). They are preserved unchanged and must be treated as source-data anomalies, not silently corrected. Any metric runner must either apply a documented tie/anomaly policy or report those cases separately.

## Ranking Metrics

For each annotated resume, the current matcher should score all five vacancies and rank them by the actual current system score. The primary ground truth remains the original human ranking, not a fabricated 0-5 relevance score.

NDCG may use a temporary monotonic relevance transformation only for metric calculation, for example `relevance = 6 - rank` for valid ranks 1 through 5. That derived value is not stored as an original human label. Spearman compares ranking positions where defined. No MAE against the original ranks is used unless a separate, documented metric is introduced.

The current production baseline remains keyword weight `0.50` plus semantic weight `0.50`. Semantic values must come from the existing semantic layer; unavailable semantic infrastructure must not be replaced with zeros, keyword scores, estimates, or copied final scores.

## Scope

The repository does not commit the source DOCX resumes or vacancy text. The source files should be downloaded into an evaluation-only working directory when running the benchmark. The checked-in manifest is the human-ranking ground truth and source mapping, not a redistribution of the source documents.

Human-ranking benchmark results are not universal accuracy claims. They describe this dataset, its population, and its annotation process only.

## Current Run Status

The checked-in `human-ranking-report.json` is explicitly marked `NOT_RUN`. The official source archive was obtained temporarily, but the production semantic/pgvector runtime was unavailable, so no current 50/50 baseline scores, system rankings, NDCG values, or Spearman values were calculated. The report therefore contains null metrics and zero evaluated pairs. This is intentional and must not be replaced with keyword-only, synthetic, or estimated results.
