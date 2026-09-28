# Evaluation Data

This directory is for human-labeled resume/JD evaluation examples. The expected JSON format is an array of objects:

```json
[
  {
    "id": "java-jd-001-resume-001",
  {
    "pairId": "java-jd-001-pair-001",
    "jobId": "java-jd-001",
    "resumeId": "resume-001",
    "humanRelevanceScore": 5
  }
]
```

`humanRelevanceScore` is ground truth on this scale:

- `0`: completely irrelevant
- `1`: very weak match

The independent evaluation runner preserves the production `keywordScore * 0.50 + semanticScore * 0.50` baseline when a real semantic score is available. When semantic infrastructure is unavailable, semantic and baseline scores remain unavailable rather than being fabricated. Diagnostic component scores remain available.

Metrics include normalized MAE, tie-aware Spearman correlation, Pearson correlation, and graded NDCG@1, @3, and @5. Correlations require at least two non-constant observations. Ranking metrics group examples by explicit `jobId`; enough real labeled examples per job group are required before interpreting results as meaningful.

Evaluation code can be exercised with Maven tests; production controller and scoring paths are intentionally not integrated yet.