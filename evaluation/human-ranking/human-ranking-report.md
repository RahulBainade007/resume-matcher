# Human-Ranking Benchmark Results

Status: evaluated against the current production baseline.

## Dataset

- Vacancies: 5
- Annotated resumes: 30
- Expected pairs: 150
- Evaluated pairs: 150
- Failed pairs: 0

## Baseline

Current production baseline: keywordScore * 0.50 + semanticScore * 0.50

## Metrics

- NDCG@1: 0.41111111111111115
- NDCG@3: 0.5059572411158207
- NDCG@5: 0.725313465300266
- Spearman: -0.09409841145276211

Per-resume results are stored in the JSON report. Human rankings are preserved and were not modified.
